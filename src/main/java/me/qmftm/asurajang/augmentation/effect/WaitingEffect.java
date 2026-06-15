package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class WaitingEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onRegainHealth(Player player, EntityRegainHealthEvent event) {
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.REGEN) return;

        double multiplier = AugmentSettings.getDouble("Waiting", "regen-multiplier", 3.0);
        event.setAmount(event.getAmount() * multiplier);
    }
}
