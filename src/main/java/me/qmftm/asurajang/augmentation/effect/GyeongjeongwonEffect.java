package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GyeongjeongwonEffect implements AugmentationEffect {

    // AugmentationEffectListener에서 참조해 2배 넉백 처리
    public static final Set<UUID> pendingDoubleKnockback = ConcurrentHashMap.newKeySet();
    // 2타 재귀 방지
    private static final Set<UUID> secondaryHit = ConcurrentHashMap.newKeySet();

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (secondaryHit.contains(player.getUniqueId())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        double original = event.getDamage();
        event.setDamage(original * 0.8);

        Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (!target.isValid() || target.isDead()) return;
            secondaryHit.add(player.getUniqueId());
            pendingDoubleKnockback.add(player.getUniqueId());
            target.damage(original * 0.4, player);
            secondaryHit.remove(player.getUniqueId());
        }, 10L); // 0.5초
    }
}
