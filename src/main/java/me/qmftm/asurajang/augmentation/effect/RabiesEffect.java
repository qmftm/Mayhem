package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RabiesEffect implements AugmentationEffect, Listener {

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWolfAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Wolf wolf)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(wolf.getOwner() instanceof Player wolfOwner) || !wolfOwner.equals(owner)) return;
        if (target.equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        long duration = AugmentSettings.getLong("Rabies", "wolf-charm-duration-ticks", 20L);
        double slow = AugmentSettings.getDouble("Rabies", "slow-amount", 0.5);

        CharmEffect.applyCharm(target, owner, duration, slow);

        target.getWorld().spawnParticle(Particle.WITCH, target.getLocation().add(0, 1.5, 0), 8, 0.3, 0.3, 0.3);
        target.playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 0.8f);
    }
}
