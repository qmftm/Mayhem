package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;

public class BoogieWoogieEffect implements AugmentationEffect {

    private static final double RANGE_SQ   = 20.0 * 20.0; // 20블록 이내 (거리² 비교)
    private static final long   COOLDOWN_MS = 5_000;

    private long lastUsed = 0;
    private BukkitTask actionBarTask;

    @Override
    public void onActivate(Player player) {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (!player.isOnline()) return;
            long elapsed = System.currentTimeMillis() - lastUsed;
            if (elapsed < COOLDOWN_MS) {
                long remaining = (COOLDOWN_MS - elapsed + 999) / 1000;
                player.sendActionBar(Component.text("부기우기 쿨타임: " + remaining + "초", NamedTextColor.RED));
            }
        }, 0L, 20L);
    }

    @Override
    public void onDeactivate(Player player) {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }

    @Override
    public void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastUsed < COOLDOWN_MS) return;

        // 20블록 내 가장 가까운 생존 플레이어 탐색
        Player target = null;
        double minDistSq = RANGE_SQ;

        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player) || p.getGameMode() == GameMode.SPECTATOR) continue;
            double distSq = p.getLocation().distanceSquared(player.getLocation());
            if (distSq < minDistSq) {
                minDistSq = distSq;
                target = p;
            }
        }

        if (target == null) return;

        event.setCancelled(true); // 아이템 교환 방지

        final Player finalTarget = target;
        Asurajang plugin = Asurajang.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location playerLoc = player.getLocation().clone();
            Location targetLoc = finalTarget.getLocation().clone();

            Location newPlayerLoc = targetLoc.clone();
            newPlayerLoc.setYaw(playerLoc.getYaw());
            newPlayerLoc.setPitch(playerLoc.getPitch());

            Location newTargetLoc = playerLoc.clone();
            newTargetLoc.setYaw(targetLoc.getYaw());
            newTargetLoc.setPitch(targetLoc.getPitch());

            player.teleport(newPlayerLoc);
            finalTarget.teleport(newTargetLoc);

            // 박수 효과음: 스네어 + 하이햇 레이어를 3회 연속 (짝짝짝)
            long[] delays = {0L, 3L, 6L};
            for (long delay : delays) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    playerLoc.getWorld().playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_SNARE, 0.9f, 1.8f);
                    playerLoc.getWorld().playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_HAT,   0.5f, 2.0f);
                    targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_NOTE_BLOCK_SNARE, 0.9f, 1.8f);
                    targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_NOTE_BLOCK_HAT,   0.5f, 2.0f);
                }, delay);
            }
        });

        lastUsed = now;
    }
}
