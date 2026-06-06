package me.qmftm.asurajang.augmentation.effect;

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

        double maxHp = Objects.requireNonNull(victim.getAttribute(Attribute.MAX_HEALTH)).getValue();
        double hpPercent = victim.getHealth() / maxHp * 100.0;

        if (hpPercent > 50.0) {
            event.setDamage(event.getDamage() * 0.5);
        } else {
            double multiplier = 1.0 + (50.0 - hpPercent) / 100.0;
            event.setDamage(event.getDamage() * multiplier);
        }
    }
}
