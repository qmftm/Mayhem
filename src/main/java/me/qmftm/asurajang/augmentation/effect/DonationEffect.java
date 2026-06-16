package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;

public class DonationEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        int amount = AugmentSettings.getInt("Donation", "gold-amount", 450);
        Asurajang.getInstance().getScoreboardManager().addGold(player, amount);
    }

    @Override
    public void onDeactivate(Player player) {}
}
