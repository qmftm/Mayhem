package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private static final int KILL_GOLD_REWARD = 50;
    private static final int FIRST_BLOOD_BONUS = 25;
    private static final long MULTI_KILL_WINDOW_MS = 10_000L;

    private final Map<UUID, Location> deathLocations = new HashMap<>();
    private final Map<UUID, Integer> multiKillCounts = new HashMap<>();
    private final Map<UUID, Long>    lastKillTimes   = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);

        Player player = event.getPlayer();
        deathLocations.put(player.getUniqueId(), player.getLocation().clone());

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

            Asurajang.getInstance().getScoreboardManager().addKill(killer);
            Asurajang.getInstance().getScoreboardManager().addGold(killer, reward);
            killer.playSound(killer.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.8f);

            Component message = Component.text()
                .append(killer.displayName())
                .append(Component.text("님이 ", NamedTextColor.GRAY))
                .append(player.displayName())
                .append(Component.text("님을 처치했습니다", NamedTextColor.GRAY))
                .append(firstBlood
                    ? Component.text(". 퍼스트 블러드!", NamedTextColor.RED)
                    : Component.empty())
                .build();
            event.deathMessage(message);

            killer.sendMessage(Component.text()
                .append(multiKillLabel(multi))
                .append(Component.text("+" + reward + " 골드", NamedTextColor.GOLD))
                .append(firstBlood
                    ? Component.text(" (선취점 보너스)", NamedTextColor.GRAY)
                    : Component.empty())
                .build());
        }

        Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(),
            () -> Asurajang.getVersionAdapter().respawnPlayer(player), 1L);
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
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
                return;
            }

            player.showTitle(Title.title(
                Component.text(timer[0] + "초", NamedTextColor.YELLOW),
                Component.text("잠시 후 리스폰됩니다", NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
            ));
            timer[0]--;
        }, 0L, 20L);
    }
}
