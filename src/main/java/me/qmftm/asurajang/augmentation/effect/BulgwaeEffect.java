package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.concurrent.ThreadLocalRandom;

public class BulgwaeEffect implements AugmentationEffect, Listener {

    private static final double NEGATE_CHANCE = 0.75;

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
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!event.getPlayer().equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        if (ThreadLocalRandom.current().nextDouble() < NEGATE_CHANCE) {
            event.setCancelled(true);
        }
    }
}
