package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DragonSlayerEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        boolean hasDragon = Asurajang.getInstance().getAugmentationManager()
            .getActiveEffects(victim.getUniqueId()).containsKey("Dragon");
        if (!hasDragon) return;

        double multiplier = AugmentSettings.getDouble("DragonSlayer", "damage-multiplier", 2.0);
        event.setDamage(event.getDamage() * multiplier);
    }
}
