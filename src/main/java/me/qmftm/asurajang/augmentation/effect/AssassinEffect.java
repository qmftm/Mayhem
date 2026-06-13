package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

public class AssassinEffect implements AugmentationEffect, Listener {

    private Player owner;
    private BukkitTask reEnableTask;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
        addModifier(player);
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        if (reEnableTask != null) {
            reEnableTask.cancel();
            reEnableTask = null;
        }
        removeModifier(player);
        owner = null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;

        removeModifier(player);
        if (reEnableTask != null) reEnableTask.cancel();

        long disableDuration = AugmentSettings.getLong("Assassin", "disable-duration-ticks", 120L);
        reEnableTask = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            if (player.isOnline()) addModifier(player);
        }, disableDuration);
    }

    private void addModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        removeModifier(player);
        double bonus = AugmentSettings.getDouble("Assassin", "speed-bonus", 1.0);
        attr.addModifier(new AttributeModifier(key(), bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    private void removeModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key()))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }

    private static NamespacedKey key() {
        return new NamespacedKey(Asurajang.getInstance(), "assassin_speed");
    }
}
