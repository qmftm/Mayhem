package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BlinkEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {
        long cooldownTicks = AugmentSettings.getLong("Blink", "cooldown-ticks", 60L);
        double distance = AugmentSettings.getDouble("Blink", "distance", 7.0);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < cooldownTicks) {
            long remain = (cooldownTicks - (now - lastUsed) + 19) / 20;
            player.sendMessage(Component.text("[점멸] ", NamedTextColor.YELLOW)
                    .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }

        event.setCancelled(true);
        lastUsed = now;

        Location origin = player.getLocation();
        double yaw = Math.toRadians(origin.getYaw());
        Vector direction = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));

        Location destination = origin.clone();
        double step = 0.5;
        for (double d = step; d <= distance; d += step) {
            Location candidate = origin.clone().add(direction.clone().multiply(d));
            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0, 1, 0).getBlock();
            if (feet.getType().isSolid() || head.getType().isSolid()) break;
            destination = candidate;
        }

        Asurajang plugin = Asurajang.getInstance();
        Location finalDestination = destination;
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
            player.teleport(finalDestination);
            player.getWorld().spawnParticle(Particle.END_ROD, finalDestination.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
            player.playSound(finalDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.4f);
        });

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[점멸]", NamedTextColor.YELLOW)
                        .append(Component.text("를 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, cooldownTicks);
    }
}
