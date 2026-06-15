package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MutualDestructionEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onOwnerDeath(Player player) {
        Player killer = player.getKiller();
        if (killer == null || !killer.isOnline()) return;

        double damage = AugmentSettings.getDouble("MutualDestruction", "fixed-damage", 6.0);
        killer.setHealth(Math.max(0.0, killer.getHealth() - damage));

        killer.getWorld().spawnParticle(Particle.SOUL, killer.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.8f);
    }
}
