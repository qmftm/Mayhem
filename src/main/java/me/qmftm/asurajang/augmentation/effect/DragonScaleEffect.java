package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DragonScaleEffect implements AugmentationEffect {

    private int hitCount = 0;

    @Override
    public void onActivate(Player player) {
        hitCount = 0;
    }

    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        hitCount++;
        int interval = AugmentSettings.getInt("DragonScale", "hit-interval", 4);
        if (hitCount % interval != 0) return;

        int fireTicks = AugmentSettings.getInt("DragonScale", "fire-ticks", 100);
        target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));
    }
}
