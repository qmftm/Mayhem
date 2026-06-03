package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SelfDestructEffect implements AugmentationEffect {

    private static final long   BOMB_INTERVAL = 500L; // 25초
    private static final int    FUSE_SECONDS  = 5;
    private static final double RADIUS        = 5.0;

    private BukkitTask bombTimer;
    private BukkitTask fuseTask;

    @Override
    public void onActivate(Player player) {
        scheduleBomb(player);
    }

    @Override
    public void onDeactivate(Player player) {
        if (bombTimer != null) { bombTimer.cancel(); bombTimer = null; }
        if (fuseTask  != null) { fuseTask.cancel();  fuseTask  = null; }
    }

    private void scheduleBomb(Player player) {
        Asurajang plugin = Asurajang.getInstance();
        bombTimer = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !plugin.getGameManager().isRunning()) return;
            bombTimer = null;
            startFuse(player);
        }, BOMB_INTERVAL);
    }

    private void startFuse(Player player) {
        Asurajang plugin = Asurajang.getInstance();
        int[] fuse = {FUSE_SECONDS};
        fuseTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !plugin.getGameManager().isRunning()) {
                if (fuseTask != null) { fuseTask.cancel(); fuseTask = null; }
                return;
            }
            if (fuse[0] <= 0) {
                if (fuseTask != null) { fuseTask.cancel(); fuseTask = null; }
                explode(player);
                scheduleBomb(player);
                return;
            }
            player.sendActionBar(Component.text(
                "폭발까지 " + fuse[0] + "초!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f,
                0.5f + (FUSE_SECONDS - fuse[0]) * 0.1f);
            fuse[0]--;
        }, 0L, 20L);
    }

    private static void explode(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player) || target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distance(loc) > RADIUS) continue;

            target.damage(target.getMaxHealth() * 0.2, player);

            Vector dir = target.getLocation().subtract(loc).toVector().setY(0);
            if (dir.lengthSquared() > 0.001) dir.normalize();
            target.setVelocity(dir.multiply(1.5).setY(0.4));
        }
    }
}
