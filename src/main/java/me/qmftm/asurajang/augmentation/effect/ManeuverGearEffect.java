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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

public class ManeuverGearEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onProjectileDamageAsAttacker(Player shooter, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target) || target.equals(shooter)) return;

        long cooldownTicks = AugmentSettings.getLong("ManeuverGear", "cooldown-ticks", 500L);
        long effectiveCooldown = (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(shooter));

        long now = shooter.getWorld().getGameTime();
        if (now - lastUsed < effectiveCooldown) return;
        lastUsed = now;

        Location origin = shooter.getLocation().clone();
        Location dest = target.getLocation().clone();
        dest.setYaw(origin.getYaw());
        dest.setPitch(origin.getPitch());

        Asurajang plugin = Asurajang.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            origin.getWorld().spawnParticle(Particle.CLOUD, origin.clone().add(0, 1, 0), 12, 0.3, 0.5, 0.3);
            shooter.teleport(dest);
            dest.getWorld().playSound(dest, Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.0f);
        });

        shooter.sendActionBar(Component.text("입체기동장치!", NamedTextColor.GREEN));
        ActionBarTracker.markUsed(shooter);

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (shooter.isOnline()) {
                shooter.sendActionBar(Component.text("[입체기동장치]", NamedTextColor.GREEN)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(shooter);
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, effectiveCooldown);
    }
}
