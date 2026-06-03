package me.qmftm.asurajang.augmentation.effect;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GlassCannonEffect implements AugmentationEffect {

    private static final double REDUCED_HEALTH  = 20.0 * 0.7; // 14.0 (30% 감소)
    private static final double DEFAULT_HEALTH  = 20.0;

    @Override
    public void onActivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(REDUCED_HEALTH);
            if (player.getHealth() > REDUCED_HEALTH) player.setHealth(REDUCED_HEALTH);
        }
    }

    @Override
    public void onDeactivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(DEFAULT_HEALTH);
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * 1.15);
    }
}
