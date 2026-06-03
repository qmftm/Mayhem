package me.qmftm.asurajang.augmentation.effect;

import org.bukkit.entity.Player;

public class GuiltPleasureEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onKillEnemy(Player player, Player victim) {
        double heal = player.getMaxHealth() * 0.2;
        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
    }
}
