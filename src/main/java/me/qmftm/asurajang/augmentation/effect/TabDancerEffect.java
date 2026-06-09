package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

public class TabDancerEffect implements AugmentationEffect {

    private static final double SPEED_PER_STACK = 0.02;
    private static final long IDLE_TICKS = 100L;
    private static final long DECAY_INTERVAL_TICKS = 2L;

    private int stacks = 0;
    private BukkitTask idleTimer;
    private BukkitTask decayTask;

    @Override
    public void onActivate(Player player) {
        stacks = 0;
        removeSpeedModifier(player);
    }

    @Override
    public void onDeactivate(Player player) {
        cancelTimers();
        stacks = 0;
        removeSpeedModifier(player);
    }

    @Override
    public void onOwnerDeath(Player player) {
        cancelTimers();
        stacks = 0;
        removeSpeedModifier(player);
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        stacks++;
        updateSpeed(player);
        player.sendActionBar(Component.text("탭 댄서: " + stacks, NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));
        resetIdleTimer(player);
    }

    private void resetIdleTimer(Player player) {
        if (idleTimer != null) idleTimer.cancel();
        if (decayTask != null) { decayTask.cancel(); decayTask = null; }
        idleTimer = Asurajang.getInstance().getServer().getScheduler().runTaskLater(
            Asurajang.getInstance(), () -> startDecay(player), IDLE_TICKS);
    }

    private void startDecay(Player player) {
        idleTimer = null;
        decayTask = Asurajang.getInstance().getServer().getScheduler().runTaskTimer(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) { cancelTimers(); return; }
                if (stacks <= 0) { cancelTimers(); return; }
                stacks--;
                updateSpeed(player);
                if (stacks > 0) {
                    player.sendActionBar(Component.text("탭 댄서: " + stacks, NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                }
            }, 0L, DECAY_INTERVAL_TICKS);
    }

    private void cancelTimers() {
        if (idleTimer != null) { idleTimer.cancel(); idleTimer = null; }
        if (decayTask != null) { decayTask.cancel(); decayTask = null; }
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
            attr.addModifier(new AttributeModifier(key, SPEED_PER_STACK * stacks, AttributeModifier.Operation.ADD_NUMBER));
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
