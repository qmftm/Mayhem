package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SpiritBombEffect implements AugmentationEffect, Listener {

    private Player owner;
    private long lastDamageTime;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        lastDamageTime = player.getWorld().getGameTime();
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        owner = null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;
        lastDamageTime = player.getWorld().getGameTime();
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        long chargeTicks = AugmentSettings.getLong("SpiritBomb", "charge-ticks", 200L);
        long now = player.getWorld().getGameTime();
        if (now - lastDamageTime < chargeTicks) return;

        double multiplier = AugmentSettings.getDouble("SpiritBomb", "critical-multiplier", 1.5);
        event.setDamage(event.getDamage() * multiplier);
        lastDamageTime = now;

        player.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
        player.sendActionBar(Component.text("원기옥", NamedTextColor.BLUE)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        ActionBarTracker.markUsed(player);
    }
}
