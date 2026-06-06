package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GlassCannonEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Multiplier(0.7); // 최대 체력 30% 감소
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * 1.15);
    }
}
