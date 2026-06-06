package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class GaebokChiEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Fixed(0.5); // 기본 최대 체력의 50%
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        if (mgr.getActiveEffects(target.getUniqueId()).containsKey("MolaMola")) return;
        if (ThreadLocalRandom.current().nextDouble() >= 0.10) return;

        mgr.deactivateSingle(player, "MolaMola");
        mgr.activateFor(target, "MolaMola");

        player.sendActionBar(Component.text("개복치 이전됨!", NamedTextColor.GREEN));
        target.sendActionBar(Component.text("개복치 감염됨!", NamedTextColor.RED));

        player.playSound(player.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 1.0f);
        target.playSound(target.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1.0f, 1.0f);
    }
}
