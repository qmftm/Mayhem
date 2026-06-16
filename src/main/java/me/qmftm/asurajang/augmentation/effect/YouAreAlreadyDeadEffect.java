package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.util.ActionBarTracker;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YouAreAlreadyDeadEffect implements AugmentationEffect, Listener {

    private static final NamespacedKey SPEED_KEY =
        new NamespacedKey(Asurajang.getInstance(), "yaard_speed");
    private static final NamespacedKey SLOW_KEY =
        new NamespacedKey(Asurajang.getInstance(), "yaard_slow");
    private static final Map<UUID, BukkitTask> slowTasks = new HashMap<>();

    private Player owner;
    private BukkitTask reEnableTask;
    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
        addSpeedModifier(player);
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        if (reEnableTask != null) { reEnableTask.cancel(); reEnableTask = null; }
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
        removeSpeedModifier(player);
        owner = null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;
        removeSpeedModifier(player);
        if (reEnableTask != null) reEnableTask.cancel();
        long disableDuration = AugmentSettings.getLong("YouAreAlreadyDead", "disable-duration-ticks", 120L);
        reEnableTask = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (player.isOnline()) addSpeedModifier(player);
        }, disableDuration);
    }

    @Override
    public void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {
        long cooldownTicks = AugmentSettings.getLong("YouAreAlreadyDead", "cooldown-ticks", 60L);
        long effectiveCooldown = (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(player));
        double range = AugmentSettings.getDouble("YouAreAlreadyDead", "range", 7.0);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < effectiveCooldown) {
            long remain = (effectiveCooldown - (now - lastUsed) + 19) / 20;
            player.sendMessage(Component.text("[넌 이미 죽어있다] ", NamedTextColor.DARK_GRAY)
                .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }

        Player target = findNearestEnemy(player, range);
        if (target == null) return;

        event.setCancelled(true);
        lastUsed = now;

        // 1 block behind target (opposite of their forward direction)
        double yawRad = Math.toRadians(target.getLocation().getYaw());
        Vector forward = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad));
        org.bukkit.Location behindLoc = target.getLocation().clone().subtract(forward);
        behindLoc.setYaw(target.getLocation().getYaw());
        behindLoc.setPitch(0);

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        player.teleport(behindLoc);
        player.getWorld().spawnParticle(Particle.PORTAL, behindLoc.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        player.playSound(behindLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.4f);

        double slowAmount = AugmentSettings.getDouble("YouAreAlreadyDead", "slow-amount", 0.4);
        long slowDuration = AugmentSettings.getLong("YouAreAlreadyDead", "slow-duration-ticks", 60L);
        applySlowToTarget(target, slowAmount, slowDuration);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.6f);

        player.sendActionBar(Component.text("넌 이미 죽어있다!", NamedTextColor.DARK_GRAY));
        ActionBarTracker.markUsed(player);

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[넌 이미 죽어있다]", NamedTextColor.DARK_GRAY)
                    .append(Component.text("를 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, effectiveCooldown);
    }

    private Player findNearestEnemy(Player player, double range) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        int playerTeam = gm.getTeam(player.getUniqueId());

        Player nearest = null;
        double nearestDistSq = range * range;
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player) || !p.isOnline() || p.isDead()) continue;
            int pTeam = gm.getTeam(p.getUniqueId());
            if (playerTeam != -1 && pTeam == playerTeam) continue;
            double distSq = p.getLocation().distanceSquared(player.getLocation());
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }

    private static void applySlowToTarget(Player target, double slowAmount, long durationTicks) {
        UUID id = target.getUniqueId();
        BukkitTask prev = slowTasks.remove(id);
        if (prev != null) prev.cancel();
        removeSlowModifier(target);

        AttributeInstance attr = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.addModifier(new AttributeModifier(SLOW_KEY, -slowAmount, AttributeModifier.Operation.ADD_SCALAR));
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            slowTasks.remove(id);
            removeSlowModifier(target);
        }, durationTicks);
        slowTasks.put(id, task);
    }

    private static void removeSlowModifier(Player target) {
        AttributeInstance attr = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(SLOW_KEY))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }

    private void addSpeedModifier(Player player) {
        removeSpeedModifier(player);
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        double bonus = AugmentSettings.getDouble("YouAreAlreadyDead", "speed-bonus", 0.6);
        attr.addModifier(new AttributeModifier(SPEED_KEY, bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    private void removeSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(SPEED_KEY))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }
}
