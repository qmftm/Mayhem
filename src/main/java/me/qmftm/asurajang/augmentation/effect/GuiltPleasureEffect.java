package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class GuiltPleasureEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onKillEnemy(Player player, Player victim) {
        double heal = player.getMaxHealth() * AugmentSettings.getDouble("GuiltPleasure", "heal-fraction", 0.2);
        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));

        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0),
            10, 0.4, 0.4, 0.4, 0);
    }
}
