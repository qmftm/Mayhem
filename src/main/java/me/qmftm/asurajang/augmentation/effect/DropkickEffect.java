package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DropkickEffect implements AugmentationEffect {

    public static final Set<UUID> pendingKnockback = ConcurrentHashMap.newKeySet();

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player attacker, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        double maxHp = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (victim.getHealth() / maxHp > 0.15) return;

        // 즉사
        event.setDamage(maxHp * 100.0);

        // 폭발 이펙트
        victim.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, victim.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.9f);

        // EntityKnockbackByEntityEvent에서 넉백 배율 적용
        pendingKnockback.add(attacker.getUniqueId());

        // 공격자 체력 50% 회복 (1틱 후)
        double healAmount = attacker.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.5;
        Asurajang.getInstance().getServer().getScheduler().runTask(Asurajang.getInstance(), () -> {
            double newHp = Math.min(
                attacker.getAttribute(Attribute.MAX_HEALTH).getValue(),
                attacker.getHealth() + healAmount);
            attacker.setHealth(newHp);
        });
    }
}
