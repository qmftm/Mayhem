package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ResilientSpiritEffect implements AugmentationEffect, Listener {

    private Player owner;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        owner = null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;

        double threshold = AugmentSettings.getDouble("ResilientSpirit", "health-threshold", 0.5);
        if (player.getHealth() / player.getMaxHealth() > threshold) return;

        double reduction = AugmentSettings.getDouble("ResilientSpirit", "damage-reduction", 0.15);
        event.setDamage(event.getDamage() * (1 - reduction));
    }
}
