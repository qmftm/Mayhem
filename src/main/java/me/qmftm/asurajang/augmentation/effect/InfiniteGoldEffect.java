package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class InfiniteGoldEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        int amount = AugmentSettings.getInt("InfiniteGold", "gold-amount", 32767);
        Asurajang.getInstance().getScoreboardManager().setGold(player, amount);
    }

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        applyPenalty(event);
    }

    @Override
    public void onProjectileDamageAsAttacker(Player shooter, EntityDamageByEntityEvent event) {
        applyPenalty(event);
    }

    private static void applyPenalty(EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * AugmentSettings.getDouble("InfiniteGold", "damage-multiplier", 0.9));
    }

    @Override
    public double getRespawnDelayMultiplier() {
        return AugmentSettings.getDouble("InfiniteGold", "respawn-multiplier", 2.0);
    }
}
