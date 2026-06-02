package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class DeadfulEffect implements AugmentationEffect {

    private static final double DEADFUL_MAX_HEALTH = 10.0; // 5칸
    private static final double DEFAULT_MAX_HEALTH  = 20.0;

    private BukkitTask healTask;

    @Override
    public void onActivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(DEADFUL_MAX_HEALTH);
            if (player.getHealth() > DEADFUL_MAX_HEALTH) {
                player.setHealth(DEADFUL_MAX_HEALTH);
            }
        }

        healTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (player.isOnline()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, false, false));
            }
        }, 0L, 20L);
    }

    @Override
    public void onDeactivate(Player player) {
        if (healTask != null) {
            healTask.cancel();
            healTask = null;
        }
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(DEFAULT_MAX_HEALTH);
        }
    }
}
