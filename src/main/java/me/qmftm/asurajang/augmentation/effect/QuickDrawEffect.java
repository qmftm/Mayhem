package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

public class QuickDrawEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;
        removeModifier(attr);
        double bonus = AugmentSettings.getDouble("QuickDraw", "attack-speed-bonus", 0.3);
        attr.addModifier(new AttributeModifier(key(), bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    @Override
    public void onDeactivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;
        removeModifier(attr);
    }

    private static void removeModifier(AttributeInstance attr) {
        NamespacedKey key = key();
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }

    private static NamespacedKey key() {
        return new NamespacedKey(Asurajang.getInstance(), "quick_draw_attack_speed");
    }
}
