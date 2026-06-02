package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class GaebokChiEffect implements AugmentationEffect {

    private static final double HALF_HEALTH    = 10.0; // 5칸
    private static final double DEFAULT_HEALTH = 20.0;

    @Override
    public void onActivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(HALF_HEALTH);
            if (player.getHealth() > HALF_HEALTH) player.setHealth(HALF_HEALTH);
        }
    }

    @Override
    public void onDeactivate(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(DEFAULT_HEALTH);
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        if (mgr.getActiveEffects(target.getUniqueId()).containsKey("gaebokchi")) return;
        if (ThreadLocalRandom.current().nextDouble() >= 0.25) return;

        mgr.deactivateSingle(player, "gaebokchi");
        mgr.activateFor(target, "gaebokchi");

        player.sendActionBar(Component.text("개복치 이전됨!", NamedTextColor.GREEN));
        target.sendActionBar(Component.text("개복치 감염됨!", NamedTextColor.RED));
    }
}
