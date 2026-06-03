package me.qmftm.asurajang.augmentation.effect;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SniperDuelEffect implements AugmentationEffect {

    private static final double MIN_DISTANCE = 12.0;

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onProjectileDamageAsAttacker(Player shooter, EntityDamageByEntityEvent event) {
        applyBonus(shooter, event);
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        applyBonus(player, event);
    }

    private static void applyBonus(Player attacker, EntityDamageByEntityEvent event) {
        double dist = attacker.getLocation().distance(event.getEntity().getLocation());
        if (dist >= MIN_DISTANCE) {
            event.setDamage(event.getDamage() * 1.5);
        }
    }
}
