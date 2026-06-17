package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

public class NanoTechnologyEffect implements AugmentationEffect {

    private static final NamespacedKey MAX_ABSORPTION_KEY =
        new NamespacedKey(Asurajang.getInstance(), "nano_technology_max_absorption");

    private double totalAbsorptionGained = 0.0;

    @Override
    public void onActivate(Player player) {
        totalAbsorptionGained = 0.0;
    }

    @Override
    public void onDeactivate(Player player) {
        totalAbsorptionGained = 0.0;
        AttributeInstance attr = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (attr == null) return;
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(MAX_ABSORPTION_KEY))
            .findFirst()
            .ifPresent(attr::removeModifier);
        player.setAbsorptionAmount(0.0);
    }

    @Override
    public void onKillEnemy(Player player, Player victim) {
        double ratio = AugmentSettings.getDouble("NanoTechnology", "absorption-ratio", 0.5);
        double gain = victim.getMaxHealth() * ratio;
        totalAbsorptionGained += gain;

        AttributeInstance attr = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (attr != null) {
            attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(MAX_ABSORPTION_KEY))
                .findFirst()
                .ifPresent(attr::removeModifier);
            attr.addModifier(new AttributeModifier(MAX_ABSORPTION_KEY, totalAbsorptionGained, AttributeModifier.Operation.ADD_NUMBER));
        }

        player.setAbsorptionAmount(player.getAbsorptionAmount() + gain);

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.2, 0),
            16, 0.4, 0.6, 0.4, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.8f);
    }
}
