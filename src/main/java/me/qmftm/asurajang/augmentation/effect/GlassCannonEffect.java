package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GlassCannonEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Multiplier(AugmentSettings.getDouble("GlassCannon", "max-health-multiplier", 0.7));
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        double fixedDamage = event.getDamage() * AugmentSettings.getDouble("GlassCannon", "fixed-damage-ratio", 0.15);
        Asurajang.getInstance().getServer().getScheduler().runTask(Asurajang.getInstance(), () -> {
            if (!victim.isOnline() || victim.isDead()) return;
            victim.setHealth(Math.max(0, victim.getHealth() - fixedDamage));
        });
    }
}
