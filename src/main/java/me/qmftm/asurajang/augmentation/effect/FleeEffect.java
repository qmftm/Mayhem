package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class FleeEffect implements AugmentationEffect {

    private BukkitTask task;

    @Override
    public void onActivate(Player player) {
        double threshold = AugmentSettings.getDouble("Flee", "health-threshold", 0.35);
        int amplifier = AugmentSettings.getInt("Flee", "speed-amplifier", 0);
        int duration = AugmentSettings.getInt("Flee", "effect-duration-ticks", 60);
        long checkInterval = AugmentSettings.getLong("Flee", "check-interval-ticks", 20L);

        task = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (!player.isOnline()) return;
            if (player.getHealth() / player.getMaxHealth() <= threshold) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier, false, false));
            }
        }, 0L, checkInterval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
