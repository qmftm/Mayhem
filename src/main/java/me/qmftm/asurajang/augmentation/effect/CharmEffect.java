package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarCountdown;
import me.qmftm.asurajang.util.ActionBarTracker;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CharmEffect implements AugmentationEffect {

    private static final NamespacedKey SLOW_KEY = new NamespacedKey(Asurajang.getInstance(), "charm_slow");
    private static final Map<UUID, BukkitTask> activeGaze = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> activeRemoval = new ConcurrentHashMap<>();

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onDamageAsAttacker(Player attacker, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target) || target.equals(attacker)) return;

        long cooldownTicks = AugmentSettings.getLong("Charm", "cooldown-ticks", 500L);
        long effectiveCooldown = (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(attacker));

        long now = attacker.getWorld().getGameTime();
        if (now - lastUsed < effectiveCooldown) return;
        lastUsed = now;

        long durationTicks = AugmentSettings.getLong("Charm", "duration-ticks", 60L);
        double slowAmount = AugmentSettings.getDouble("Charm", "slow-amount", 0.5);

        applyCharm(target, attacker, durationTicks, slowAmount);

        target.getWorld().spawnParticle(Particle.WITCH, target.getLocation().add(0, 1.5, 0), 12, 0.3, 0.3, 0.3);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 1.2f);

        attacker.sendActionBar(Component.text("매혹!", NamedTextColor.LIGHT_PURPLE));
        ActionBarTracker.markUsed(attacker);

        Asurajang plugin = Asurajang.getInstance();
        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (attacker.isOnline()) {
                attacker.sendActionBar(Component.text("[매혹]", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(attacker);
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, effectiveCooldown);
    }

    static void applyCharm(Player target, Player attacker, long durationTicks, double slowAmount) {
        UUID targetId = target.getUniqueId();
        Asurajang plugin = Asurajang.getInstance();

        BukkitTask prevGaze = activeGaze.remove(targetId);
        if (prevGaze != null) prevGaze.cancel();
        BukkitTask prevRemoval = activeRemoval.remove(targetId);
        if (prevRemoval != null) prevRemoval.cancel();

        removeSlow(target);
        AttributeInstance attr = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.addModifier(new AttributeModifier(SLOW_KEY, -slowAmount, AttributeModifier.Operation.ADD_SCALAR));
        }

        long[] remainingTicks = {durationTicks};
        BukkitTask gazeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!target.isOnline()) return;
            if (attacker.isOnline()) target.lookAt(attacker.getEyeLocation(), LookAnchor.EYES);
            ActionBarCountdown.show(target, "매혹", NamedTextColor.LIGHT_PURPLE, remainingTicks[0]);
            remainingTicks[0]--;
        }, 0L, 1L);
        activeGaze.put(targetId, gazeTask);

        BukkitTask removalTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BukkitTask gaze = activeGaze.remove(targetId);
            if (gaze != null) gaze.cancel();
            activeRemoval.remove(targetId);
            removeSlow(target);
        }, durationTicks);
        activeRemoval.put(targetId, removalTask);
    }

    private static void removeSlow(Player target) {
        AttributeInstance attr = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(SLOW_KEY))
                .findFirst()
                .ifPresent(attr::removeModifier);
    }
}
