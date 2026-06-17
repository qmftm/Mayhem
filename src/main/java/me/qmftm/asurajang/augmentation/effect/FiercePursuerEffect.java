package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class FiercePursuerEffect implements AugmentationEffect {

    private UUID targetId = null;
    private int stacks = 0;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        targetId = null;
        stacks = 0;
    }

    @Override
    public void onKillEnemy(Player player, Player victim) {
        stacks = stacks / 2;
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int maxStacks = AugmentSettings.getInt("FiercePursuer", "max-stacks", 30);
        double damagePerStack = AugmentSettings.getDouble("FiercePursuer", "damage-per-stack", 0.03);

        UUID entityId = target.getUniqueId();
        if (entityId.equals(targetId)) {
            if (stacks < maxStacks) stacks++;
        } else {
            targetId = entityId;
            stacks = 1;
        }

        event.setDamage(event.getDamage() * (1.0 + stacks * damagePerStack));
    }
}
