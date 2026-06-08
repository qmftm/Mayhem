package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SniperDuelEffect implements AugmentationEffect {

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
        if (dist >= AugmentSettings.getDouble("SniperDuel", "min-distance", 12.0)) {
            event.setDamage(event.getDamage() * AugmentSettings.getDouble("SniperDuel", "damage-multiplier", 1.5));
        }
    }
}
