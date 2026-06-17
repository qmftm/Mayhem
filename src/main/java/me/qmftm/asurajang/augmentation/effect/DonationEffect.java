package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DonationEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        int amount = AugmentSettings.getInt("Donation", "gold-amount", 450);
        Asurajang.getInstance().getScoreboardManager().addGold(player, amount);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1.5, 0),
            10, 0.4, 0.4, 0.4, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.8f, 1.2f);
    }

    @Override
    public void onDeactivate(Player player) {}
}
