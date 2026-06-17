package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DropkickEffect implements AugmentationEffect {

    public static final Set<UUID> pendingKnockback = ConcurrentHashMap.newKeySet();

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player attacker, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        var maxHealthAttr = victim.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) return;
        double maxHp = maxHealthAttr.getValue();
        if (victim.getHealth() / maxHp > 0.15) return;

        // 즉사
        event.setDamage(maxHp * 100.0);

        // 폭발 이펙트 + 철퇴 소리
        victim.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, victim.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.9f);
        attacker.playSound(attacker.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 1.0f, 1.0f);

        // EntityKnockbackByEntityEvent에서 넉백 배율 적용
        pendingKnockback.add(attacker.getUniqueId());

        // 처형 액션바
        attacker.sendActionBar(Component.text("처형", NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        ActionBarTracker.markUsed(attacker);

        // 공격자 체력 50% 회복 + 하트 파티클 (1틱 후)
        var attackerMaxHealth = attacker.getAttribute(Attribute.MAX_HEALTH);
        if (attackerMaxHealth == null) return;
        double healAmount = attackerMaxHealth.getValue() * 0.5;
        Asurajang.getInstance().getServer().getScheduler().runTask(Asurajang.getInstance(), () -> {
            var atkMaxHp = attacker.getAttribute(Attribute.MAX_HEALTH);
            if (atkMaxHp == null) return;
            double newHp = Math.min(atkMaxHp.getValue(), attacker.getHealth() + healAmount);
            attacker.setHealth(newHp);

            ThreadLocalRandom rng = ThreadLocalRandom.current();
            Location base = attacker.getLocation().add(0, 1, 0);
            for (int i = 0; i < 12; i++) {
                Location pos = base.clone().add(
                    (rng.nextDouble() - 0.5) * 1.2,
                    rng.nextDouble() * 1.0,
                    (rng.nextDouble() - 0.5) * 1.2);
                attacker.getWorld().spawnParticle(Particle.HEART, pos, 1, 0, 0, 0, 0);
            }
        });
    }
}
