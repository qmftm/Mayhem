package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

public class TabDancerEffect implements AugmentationEffect {

    private int stacks = 0;
    private BukkitTask idleTimer;
    private BukkitTask decayTask;
    private BukkitTask displayTask;

    @Override
    public void onActivate(Player player) {
        stacks = 0;
        removeSpeedModifier(player);
        displayTask = Asurajang.getInstance().getServer().getScheduler().runTaskTimer(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) return;
                if (stacks > 0) {
                    player.sendActionBar(Component.text("탭 댄서: " + stacks, NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                    ActionBarTracker.markUsed(player);
                }
            }, 0L, 2L);
    }

    @Override
    public void onDeactivate(Player player) {
        cancelAll();
        stacks = 0;
        removeSpeedModifier(player);
    }

    @Override
    public void onOwnerDeath(Player player) {
        cancelAll();
        stacks = 0;
        removeSpeedModifier(player);
    }

    @Override
    public void onOwnerRespawn(Player player) {
        if (displayTask == null) {
            displayTask = Asurajang.getInstance().getServer().getScheduler().runTaskTimer(
                Asurajang.getInstance(), () -> {
                    if (!player.isOnline()) return;
                    if (stacks > 0) {
                        player.sendActionBar(Component.text("탭 댄서: " + stacks, NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                        ActionBarTracker.markUsed(player);
                    }
                }, 0L, 2L);
        }
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        stacks++;
        updateSpeed(player);
        resetIdleTimer(player);
        if (stacks % 5 == 0) {
            player.getWorld().spawnParticle(Particle.NOTE, player.getLocation().add(0, 2, 0),
                3, 0.3, 0.2, 0.3, 0);
        }
    }

    private void resetIdleTimer(Player player) {
        if (idleTimer != null) idleTimer.cancel();
        if (decayTask != null) { decayTask.cancel(); decayTask = null; }
        long idleTicks = AugmentSettings.getLong("TabDancer", "idle-ticks", 100L);
        idleTimer = Asurajang.getInstance().getServer().getScheduler().runTaskLater(
            Asurajang.getInstance(), () -> startDecay(player), idleTicks);
    }

    private void startDecay(Player player) {
        idleTimer = null;
        long decayInterval = AugmentSettings.getLong("TabDancer", "decay-interval-ticks", 2L);
        decayTask = Asurajang.getInstance().getServer().getScheduler().runTaskTimer(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) { cancelTimers(); return; }
                if (stacks <= 0) { cancelTimers(); return; }
                stacks--;
                updateSpeed(player);
            }, 0L, decayInterval);
    }

    private void cancelTimers() {
        if (idleTimer != null) { idleTimer.cancel(); idleTimer = null; }
        if (decayTask != null) { decayTask.cancel(); decayTask = null; }
    }

    private void cancelAll() {
        cancelTimers();
        if (displayTask != null) { displayTask.cancel(); displayTask = null; }
    }

    private void updateSpeed(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        NamespacedKey key = speedKey();
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .findFirst()
            .ifPresent(attr::removeModifier);
        if (stacks > 0) {
            double speedPerStack = AugmentSettings.getDouble("TabDancer", "speed-per-stack", 0.02);
            attr.addModifier(new AttributeModifier(key, speedPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    private void removeSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        NamespacedKey key = speedKey();
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }

    private NamespacedKey speedKey() {
        return new NamespacedKey(Asurajang.getInstance(), "tab_dancer_speed");
    }
}
