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

    private static final double RANGE_SQ    = 20.0 * 20.0;
    private static final long   COOLDOWN_MS = 5_000;

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastUsed < COOLDOWN_MS) return;

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

        event.setCancelled(true);

        final Player finalTarget = target;
        Asurajang plugin = Asurajang.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location playerLoc = player.getLocation().clone();
            Location targetLoc = finalTarget.getLocation().clone();

            Location newPlayerLoc = targetLoc.clone().add(0, 1.0, 0);
            newPlayerLoc.setYaw(playerLoc.getYaw());
            newPlayerLoc.setPitch(playerLoc.getPitch());

            Location newTargetLoc = playerLoc.clone().add(0, 1.0, 0);
            newTargetLoc.setYaw(targetLoc.getYaw());
            newTargetLoc.setPitch(targetLoc.getPitch());

            player.teleport(newPlayerLoc);
            finalTarget.teleport(newTargetLoc);

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
        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[부기우기]", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("를 다시 사용 가능합니다", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, COOLDOWN_MS / 50);
    }
}
