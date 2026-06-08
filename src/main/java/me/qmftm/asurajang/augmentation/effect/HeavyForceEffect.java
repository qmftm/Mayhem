package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HeavyForceEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * AugmentSettings.getDouble("HeavyForce", "damage-multiplier", 1.2));
    }
}
