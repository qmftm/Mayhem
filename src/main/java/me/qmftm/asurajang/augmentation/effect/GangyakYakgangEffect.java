package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Objects;

public class GangyakYakgangEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        double thresholdPercent = AugmentSettings.getDouble("StrongWeak", "threshold-percent", 50.0);
        double highHealthMultiplier = AugmentSettings.getDouble("StrongWeak", "high-health-multiplier", 0.5);
        double lowHealthBonusScale = AugmentSettings.getDouble("StrongWeak", "low-health-bonus-scale", 100.0);

        double maxHp = Objects.requireNonNull(victim.getAttribute(Attribute.MAX_HEALTH)).getValue();
        double hpPercent = victim.getHealth() / maxHp * 100.0;

        if (hpPercent > thresholdPercent) {
            event.setDamage(event.getDamage() * highHealthMultiplier);
        } else {
            double multiplier = 1.0 + (thresholdPercent - hpPercent) / lowHealthBonusScale;
            event.setDamage(event.getDamage() * multiplier);
        }
    }
}
