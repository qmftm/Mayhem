package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.concurrent.ThreadLocalRandom;

public class UnRuinEffect implements AugmentationEffect, Listener {

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
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (player.getHealth() - event.getFinalDamage() > 0) return;
        if (ThreadLocalRandom.current().nextDouble() >= AugmentSettings.getDouble("UnRuin", "revive-chance", 0.35)) return;

        event.setCancelled(true);
        revive(player);
    }

    // 부활 위치가 월드보더 밖으로 나가지 않도록 보더 안쪽으로 보정
    private Location clampToBorder(Location loc) {
        World world = loc.getWorld();
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double half = border.getSize() / 2.0 - 1.0;

        double minX = center.getX() - half;
        double maxX = center.getX() + half;
        double minZ = center.getZ() - half;
        double maxZ = center.getZ() + half;

        Location clamped = loc.clone();
        clamped.setX(Math.min(Math.max(loc.getX(), minX), maxX));
        clamped.setZ(Math.min(Math.max(loc.getZ(), minZ), maxZ));
        return clamped;
    }

    private void revive(Player player) {
        Location deathLoc = player.getLocation().clone();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double angle = rng.nextDouble() * Math.PI * 2;
        double dist  = rng.nextDouble() * AugmentSettings.getDouble("UnRuin", "revive-radius", 10.0);

        Location target = deathLoc.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        target = clampToBorder(target);
        target.setY(target.getWorld().getHighestBlockYAt(target) + 1);
        target.setYaw(deathLoc.getYaw());
        target.setPitch(deathLoc.getPitch());

        double reviveHealthFraction = AugmentSettings.getDouble("UnRuin", "revive-health-fraction", 0.5);
        player.setHealth(Math.max(1.0, player.getMaxHealth() * reviveHealthFraction));
        player.teleport(target);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
            target.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        player.playSound(target, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text("[불멸] ", NamedTextColor.GOLD)
            .append(Component.text("죽음의 문턱에서 부활했습니다!", NamedTextColor.YELLOW)));

        for (AugmentationEffect effect :
                new java.util.ArrayList<>(Asurajang.getInstance().getAugmentationManager()
                        .getActiveEffects(player.getUniqueId()).values())) {
            effect.onOwnerRevive(player);
        }
    }
}
