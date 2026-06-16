package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class FiercePursuerEffect implements AugmentationEffect {

    private UUID targetId = null;
    private int stacks = 0;
    private long lastKillTicks = -1;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        targetId = null;
        stacks = 0;
        lastKillTicks = -1;
    }

    @Override
    public void onKillEnemy(Player player, Player victim) {
        lastKillTicks = player.getWorld().getGameTime();
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int maxStacks = AugmentSettings.getInt("FiercePursuer", "max-stacks", 10);
        double damagePerStack = AugmentSettings.getDouble("FiercePursuer", "damage-per-stack", 0.07);
        long durationTicks = AugmentSettings.getLong("FiercePursuer", "kill-bonus-duration-ticks", 160L);
        double killMultiplier = AugmentSettings.getDouble("FiercePursuer", "kill-bonus-multiplier", 1.5);

        UUID entityId = target.getUniqueId();
        if (entityId.equals(targetId)) {
            if (stacks < maxStacks) stacks++;
        } else {
            targetId = entityId;
            stacks = 1;
        }

        double damage = event.getDamage() * (1.0 + stacks * damagePerStack);

        if (lastKillTicks >= 0 && player.getWorld().getGameTime() - lastKillTicks <= durationTicks) {
            damage *= killMultiplier;
        }

        event.setDamage(damage);
    }
}
