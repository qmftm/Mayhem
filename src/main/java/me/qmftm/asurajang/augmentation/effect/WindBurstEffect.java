package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class WindBurstEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onRightClick(Player player, PlayerInteractEvent event) {
        if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD")) return;

        long cooldownTicks = AugmentSettings.getLong("WindBurst", "cooldown-ticks", 400L);
        double velocityMultiplier = AugmentSettings.getDouble("WindBurst", "velocity-multiplier", 2.5);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < cooldownTicks) {
            long remain = (cooldownTicks - (now - lastUsed) + 19) / 20;
            player.sendMessage(Component.text("[붕뜨네] ", NamedTextColor.AQUA)
                    .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }
        lastUsed = now;

        event.setCancelled(true);

        Location eye = player.getEyeLocation();
        var dir = eye.getDirection();

        player.getWorld().spawn(eye.clone().add(dir), WindCharge.class, wc -> {
            wc.setShooter(player);
            wc.setVelocity(dir.clone().multiply(velocityMultiplier));
        });

        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0f, 1.0f);

        Asurajang plugin = Asurajang.getInstance();
        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[붕뜨네]", NamedTextColor.AQUA)
                    .append(Component.text("를 다시 사용 가능합니다", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, cooldownTicks);
    }
}
