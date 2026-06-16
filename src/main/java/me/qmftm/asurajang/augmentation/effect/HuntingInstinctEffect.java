package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HuntingInstinctEffect implements AugmentationEffect {

    private long lastKillTicks = -1;

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        lastKillTicks = -1;
    }

    @Override
    public void onKillEnemy(Player player, Player victim) {
        lastKillTicks = player.getWorld().getGameTime();
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (lastKillTicks < 0) return;
        long durationTicks = AugmentSettings.getLong("HuntingInstinct", "kill-bonus-duration-ticks", 160L);
        if (player.getWorld().getGameTime() - lastKillTicks > durationTicks) return;

        double multiplier = AugmentSettings.getDouble("HuntingInstinct", "kill-bonus-multiplier", 1.3);
        event.setDamage(event.getDamage() * multiplier);
    }
}
