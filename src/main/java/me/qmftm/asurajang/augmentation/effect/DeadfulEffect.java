package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class DeadfulEffect implements AugmentationEffect {

    private BukkitTask healTask;

    @Override
    public void onActivate(Player player) {
        long healInterval = AugmentSettings.getLong("Deadful", "heal-interval-ticks", 20L);
        healTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (player.isOnline()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, false, false));
            }
        }, 0L, healInterval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (healTask != null) {
            healTask.cancel();
            healTask = null;
        }
    }

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Fixed(AugmentSettings.getDouble("Deadful", "max-health-fixed", 0.5));
    }
}
