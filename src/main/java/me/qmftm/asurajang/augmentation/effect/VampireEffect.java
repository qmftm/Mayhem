package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class VampireEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        double healRatio = AugmentSettings.getDouble("Vampire", "heal-ratio", 0.10);
        double heal = event.getFinalDamage() * healRatio;
        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
    }
}
