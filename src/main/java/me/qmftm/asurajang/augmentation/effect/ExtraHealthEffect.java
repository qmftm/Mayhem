package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.entity.Player;

public class ExtraHealthEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Additive(AugmentSettings.getDouble("ExtraHealth", "bonus-health", 10.0));
    }
}
