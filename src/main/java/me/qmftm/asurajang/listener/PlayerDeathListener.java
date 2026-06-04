package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.effect.HeugsomEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDeathListener implements Listener {

    private static final int KILL_GOLD_REWARD = 50;
    private static final int FIRST_BLOOD_BONUS = 25;
    private static final long MULTI_KILL_WINDOW_MS = 10_000L;

    private static final long ASSIST_WINDOW_MS = 10_000L;

    private final Map<UUID, Location>           deathLocations  = new HashMap<>();
    private final Map<UUID, Integer>            multiKillCounts = new HashMap<>();
    private final Map<UUID, Long>               lastKillTimes   = new HashMap<>();
    // victim UUID → (attacker UUID → last hit timestamp)
    private final Map<UUID, Map<UUID, Long>>    recentDamage    = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        recentDamage
            .computeIfAbsent(victim.getUniqueId(), k -> new HashMap<>())
            .put(attacker.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHealthChange(EntityDamageEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        scheduleTabListUpdate(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        scheduleTabListUpdate(player);
    }

    private void scheduleTabListUpdate(Player player) {
        Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (player.isOnline() && Asurajang.getInstance().getGameManager().isRunning()) {
                Asurajang.getInstance().getScoreboardManager().updateTabListEntry(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);

        Player player = event.getPlayer();
        deathLocations.put(player.getUniqueId(), player.getLocation().clone());
        Asurajang.getInstance().getScoreboardManager().addDeath(player);

        // 킬 추적
        Player killer = player.getKiller();
        if (killer != null) {
            boolean firstBlood = Asurajang.getInstance().getGameManager().claimFirstBlood();

            // 연속 킬 카운트 (10초 내)
            long now = System.currentTimeMillis();
            UUID kid = killer.getUniqueId();
            int multi = (now - lastKillTimes.getOrDefault(kid, 0L) < MULTI_KILL_WINDOW_MS)
                ? multiKillCounts.getOrDefault(kid, 0) + 1
                : 1;
            multiKillCounts.put(kid, multi);
            lastKillTimes.put(kid, now);

            int reward = KILL_GOLD_REWARD + (firstBlood ? FIRST_BLOOD_BONUS : 0) + multiKillBonus(multi);

            // 어시스터 수집 (킬 보상 분배를 위해 먼저 계산)
            Map<UUID, Long> damagers = recentDamage.remove(player.getUniqueId());
            List<Player> assisters = new ArrayList<>();
            if (damagers != null) {
                for (Map.Entry<UUID, Long> entry : damagers.entrySet()) {
                    if (entry.getKey().equals(kid)) continue;
                    if (now - entry.getValue() > ASSIST_WINDOW_MS) continue;
                    Player assister = Bukkit.getPlayer(entry.getKey());
                    if (assister == null || !assister.isOnline()) continue;
                    assisters.add(assister);
                }
            }

            // 어시스트가 있으면 킬러는 절반, 어시스터는 총량의 1/n
            int killerGold   = assisters.isEmpty() ? reward : reward / 2;
            int assisterGold = assisters.isEmpty() ? 0      : reward / assisters.size();

            Asurajang plugin = Asurajang.getInstance();
            plugin.getScoreboardManager().addKill(killer);
            plugin.getScoreboardManager().addGold(killer, killerGold);
            killer.playSound(killer.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.8f);

            Component message = Component.text()
                .append(killer.displayName())
                .append(Component.text("님이 ", NamedTextColor.GRAY))
                .append(player.displayName())
                .append(Component.text("님을 처치했습니다", NamedTextColor.GRAY))
                .append(firstBlood
                    ? Component.text(". ", NamedTextColor.GRAY)
                        .append(Component.text("퍼스트 블러드!", NamedTextColor.RED))
                    : Component.empty())
                .build();
            event.deathMessage(message);

            final Component goldMsg = buildGoldMessage(multi, firstBlood, killerGold, !assisters.isEmpty());
            Bukkit.getScheduler().runTaskLater(plugin, () -> killer.sendMessage(goldMsg), 1L);

            // 어시스터 처리
            for (Player assister : assisters) {
                plugin.getScoreboardManager().addAssist(assister);
                plugin.getScoreboardManager().addGold(assister, assisterGold);
                final int ag = assisterGold;
                assister.sendMessage(Component.text()
                    .append(Component.text("어시스트 ", NamedTextColor.AQUA))
                    .append(player.displayName())
                    .append(Component.text("  +" + ag + " 골드", NamedTextColor.GOLD))
                    .build());
            }
        } else {
            recentDamage.remove(player.getUniqueId());
        }

        // 흑섬 발동으로 사망 시 파티클
        UUID procAttacker = HeugsomEffect.pendingDeathParticle.remove(player.getUniqueId());
        if (procAttacker != null && killer != null && killer.getUniqueId().equals(procAttacker)) {
            spawnHeugsomDeathBurst(player.getLocation(), player.getWorld());
        }

        Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> player.spigot().respawn(), 1L);
    }

    private static void spawnHeugsomDeathBurst(Location loc, World world) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Particle.DustTransition transA = new Particle.DustTransition(Color.BLACK, Color.RED,   8.25f);
        Particle.DustTransition transB = new Particle.DustTransition(Color.RED,   Color.BLACK, 8.25f);
        Particle.DustOptions bigRed   = new Particle.DustOptions(Color.RED,   3.3f);
        Particle.DustOptions bigBlack = new Particle.DustOptions(Color.BLACK, 3.3f);

        Location center = loc.clone().add(0, 1, 0);

        // 산포
        for (int i = 0; i < 35; i++) {
            double offX = (rng.nextDouble() - 0.5) * 6.0;
            double offZ = (rng.nextDouble() - 0.5) * 6.0;
            double offY = rng.nextDouble() * 3.0;
            Location pos = center.clone().add(offX, offY, offZ);
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos, 1, 0, 0, 0, 0.0,
                rng.nextBoolean() ? transA : transB);
            world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0.0, bigRed);
        }

        // 중심 집중 폭발
        world.spawnParticle(Particle.DUST, center, 50, 0.6, 0.6, 0.6, 0.0, bigBlack);
        world.spawnParticle(Particle.DUST, center, 40, 0.4, 0.4, 0.4, 0.0, bigRed);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        Location deathLoc = deathLocations.remove(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (!player.isOnline() || !Asurajang.getInstance().getGameManager().isRunning()) return;

            player.setGameMode(GameMode.SPECTATOR);
            if (deathLoc != null) player.teleport(deathLoc);

            startSpectatorTimer(player);
        }, 1L);
    }

    private static int multiKillBonus(int count) {
        return switch (count) {
            case 2 -> 5;
            case 3 -> 10;
            case 4 -> 15;
            case 5 -> 20;
            default -> count >= 6 ? 25 : 0;
        };
    }

    private static Component buildGoldMessage(int multi, boolean firstBlood, int total, boolean hasAssist) {
        int multiBonus = multiKillBonus(multi);
        Component.Builder b = Component.text();
        b.append(multiKillLabel(multi));
        b.append(Component.text("+" + total + " 골드 획득", NamedTextColor.GOLD));

        List<String> reasons = new ArrayList<>();
        if (firstBlood) reasons.add("선취점 +" + FIRST_BLOOD_BONUS);
        if (multiBonus > 0) reasons.add(multiKillName(multi) + " +" + multiBonus);
        if (hasAssist) reasons.add("어시스트 분배");

        if (!reasons.isEmpty()) {
            b.append(Component.text(" (" + String.join(", ", reasons) + ")", NamedTextColor.GRAY));
        }
        return b.build();
    }

    private static String multiKillName(int count) {
        return switch (count) {
            case 2 -> "더블킬";
            case 3 -> "트리플킬";
            case 4 -> "쿼드라킬";
            case 5 -> "펜타킬";
            default -> "전설적인킬";
        };
    }

    private static Component multiKillLabel(int count) {
        return switch (count) {
            case 2 -> Component.text("더블 킬 ", NamedTextColor.YELLOW);
            case 3 -> Component.text("트리플 킬 ", NamedTextColor.GOLD);
            case 4 -> Component.text("쿼드라 킬 ", NamedTextColor.RED);
            case 5 -> Component.text("펜타 킬 ", NamedTextColor.LIGHT_PURPLE);
            default -> count >= 6
                ? Component.text("전설적인 킬 ", NamedTextColor.AQUA)
                : Component.empty();
        };
    }

    private void startSpectatorTimer(Player player) {
        Asurajang plugin = Asurajang.getInstance();
        int[] timer = {10};

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || !plugin.getGameManager().isRunning()) {
                task.cancel();
                return;
            }

            if (timer[0] <= 0) {
                task.cancel();
                player.setGameMode(GameMode.SURVIVAL);
                Location spawn = plugin.getBattlefieldManager().getRandomSpawn();
                player.teleport(spawn != null ? spawn : player.getWorld().getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.4f);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 1.2f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.1f);
                return;
            }

            // 카운트다운 틱 효과음 (마지막 3초는 피치 올라감)
            float pitch = timer[0] <= 3 ? 1.2f + (3 - timer[0]) * 0.2f : 0.8f;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, pitch);

            player.showTitle(Title.title(
                Component.text(timer[0] + "초", NamedTextColor.YELLOW),
                Component.text("잠시 후 리스폰됩니다", NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
            ));
            timer[0]--;
        }, 0L, 20L);
    }
}
