package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class VengefulGhostEffect implements AugmentationEffect, Listener {

    private Player owner;
    private UUID lastKillerUUID;
    private double lastDamage;

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
        lastKillerUUID = null;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Entity damager = event.getDamager();
        Player attacker = null;
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }
        if (attacker == null) return;

        lastKillerUUID = attacker.getUniqueId();
        lastDamage = event.getFinalDamage();
    }

    @Override
    public void onOwnerRevive(Player player) {
        if (lastKillerUUID == null) return;

        Player killer = Asurajang.getInstance().getServer().getPlayer(lastKillerUUID);
        lastKillerUUID = null;
        if (killer == null || !killer.isOnline() || killer.getHealth() <= 0) return;

        double dmg = lastDamage;
        killer.setHealth(Math.max(0.0, killer.getHealth() - dmg));

        killer.getWorld().spawnParticle(Particle.SOUL,
            killer.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.05);
        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.7f);

        player.sendMessage(Component.text("[복수귀] ", NamedTextColor.DARK_PURPLE)
            .append(Component.text("죽인 적에게 복수했습니다!", NamedTextColor.LIGHT_PURPLE)));
    }
}
