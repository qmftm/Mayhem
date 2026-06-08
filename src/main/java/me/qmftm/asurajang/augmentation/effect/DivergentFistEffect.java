package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DivergentFistEffect implements AugmentationEffect {

    // AugmentationEffectListener에서 참조해 2배 넉백 처리
    public static final Set<UUID> pendingDoubleKnockback = ConcurrentHashMap.newKeySet();
    // 이번 타격에서 경정권이 실제로 발동했는지 (쿨타임 중이 아닌지) AugmentationEffectListener가 확인용으로 참조
    // → 발동했을 때만 같은 타격에서 흑섬을 막음 (쿨타임 중에는 흑섬이 정상적으로 터질 수 있음)
    public static final Set<UUID> activatedOnThisHit = ConcurrentHashMap.newKeySet();
    // 2타 재귀 방지
    private static final Set<UUID> secondaryHit = ConcurrentHashMap.newKeySet();

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (secondaryHit.contains(player.getUniqueId())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        long cooldownTicks = AugmentSettings.getLong("DivergentFist", "cooldown-ticks", 300L);
        double firstHitMultiplier = AugmentSettings.getDouble("DivergentFist", "first-hit-multiplier", 0.8);
        double secondHitMultiplier = AugmentSettings.getDouble("DivergentFist", "second-hit-multiplier", 0.4);
        long delayTicks = AugmentSettings.getLong("DivergentFist", "delay-ticks", 10L);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < cooldownTicks) return;
        lastUsed = now;
        activatedOnThisHit.add(player.getUniqueId());

        double original = event.getDamage();
        event.setDamage(original * firstHitMultiplier);

        Asurajang plugin = Asurajang.getInstance();

        // 1틱 후 생존 확인: 첫 타로 죽은 경우 발동하지 않음
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!target.isValid() || target.isDead()) return;

            target.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!target.isValid() || target.isDead()) return;
                secondaryHit.add(player.getUniqueId());
                pendingDoubleKnockback.add(player.getUniqueId());
                target.setNoDamageTicks(0);
                target.damage(original * secondHitMultiplier, player);
                secondaryHit.remove(player.getUniqueId());
            }, delayTicks);
        });

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[경정권]", NamedTextColor.AQUA)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, cooldownTicks);
    }
}
