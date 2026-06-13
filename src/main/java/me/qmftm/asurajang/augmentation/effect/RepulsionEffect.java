package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class RepulsionEffect implements AugmentationEffect, Listener {

    private Player owner;
    private long lastTrigger = Long.MIN_VALUE;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        lastTrigger = Long.MIN_VALUE;
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

        double threshold = AugmentSettings.getDouble("Repulsion", "health-threshold", 0.2);
        double remaining = player.getHealth() - event.getFinalDamage();
        if (remaining / player.getMaxHealth() > threshold) return;

        long cooldownTicks = AugmentSettings.getLong("Repulsion", "cooldown-ticks", 100L);
        long now = player.getWorld().getGameTime();
        if (now - lastTrigger < cooldownTicks) return;
        lastTrigger = now;

        repel(player);
    }

    private void repel(Player player) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        double radius = AugmentSettings.getDouble("Repulsion", "radius", 5.0);
        double knockbackHorizontal = AugmentSettings.getDouble("Repulsion", "knockback-horizontal", 1.5);
        double knockbackVertical = AugmentSettings.getDouble("Repulsion", "knockback-vertical", 0.4);
        Location loc = player.getLocation();

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player) || target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distance(loc) > radius) continue;
            if (gm.getGameMode() == GameManager.GameMode.TEAM
                    && gm.getTeam(target.getUniqueId()) == gm.getTeam(player.getUniqueId())) continue;

            Vector kb = target.getLocation().subtract(loc).toVector().setY(0);
            if (kb.lengthSquared() > 0.001) kb.normalize();
            target.setVelocity(target.getVelocity().add(kb.multiply(knockbackHorizontal)).setY(knockbackVertical));
        }
    }
}
