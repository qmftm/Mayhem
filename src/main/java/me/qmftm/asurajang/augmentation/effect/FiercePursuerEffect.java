package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

        if (stacks >= 10) {
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0),
                stacks / 2, 0.3, 0.3, 0.3, 0.1);
        }
        if (stacks >= 20) {
            player.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 1.5f);
        }
    }
}
