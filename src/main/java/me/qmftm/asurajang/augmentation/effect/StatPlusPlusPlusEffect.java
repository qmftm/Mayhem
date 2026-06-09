package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.entity.Player;

public class StatPlusPlusPlusEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        for (int i = 0; i < 4; i++) {
            player.getInventory().addItem(Asurajang.createStatAnvilItem());
        }
    }

    @Override
    public void onDeactivate(Player player) {}
}
