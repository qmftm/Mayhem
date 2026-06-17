package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShadowLordEffect implements AugmentationEffect, Listener {

    private Player owner;
    private final Set<UUID> souls = new HashSet<>();
    private final Map<UUID, UUID> soulSources = new HashMap<>();
    private final Set<UUID> extractedFrom = new HashSet<>();

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        for (UUID uuid : souls) {
            Entity entity = Asurajang.getInstance().getServer().getEntity(uuid);
            if (entity != null) {
                Asurajang.getInstance().getScoreboardManager().removeEntityFromTeams(entity);
                entity.remove();
            }
        }
        souls.clear();
        soulSources.clear();
        extractedFrom.clear();
        owner = null;
    }

    @Override
    public void onKillEnemy(Player player, Player victim) {
        int maxSouls = AugmentSettings.getInt("ShadowLord", "max-souls", 5);
        if (souls.size() >= maxSouls) return;
        if (extractedFrom.contains(victim.getUniqueId())) return;

        Zombie soul = spawnSoul(player, victim);
        souls.add(soul.getUniqueId());
        soulSources.put(soul.getUniqueId(), victim.getUniqueId());
        extractedFrom.add(victim.getUniqueId());
    }

    @EventHandler
    public void onSoulDeath(EntityDeathEvent event) {
        UUID dead = event.getEntity().getUniqueId();
        if (!souls.remove(dead)) return;

        UUID source = soulSources.remove(dead);
        if (source != null) extractedFrom.remove(source);

        Asurajang.getInstance().getScoreboardManager().removeEntityFromTeams(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!souls.contains(event.getEntity().getUniqueId())) return;
        if (event.getTarget() == null || owner == null) return;

        if (!(event.getTarget() instanceof Player target) || target.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        GameManager gm = Asurajang.getInstance().getGameManager();
        if (target.equals(owner) || (gm.getGameMode() == GameManager.GameMode.TEAM
                && gm.getTeam(target.getUniqueId()) == gm.getTeam(owner.getUniqueId()))) {
            event.setCancelled(true);
        }
    }

    private Zombie spawnSoul(Player summoner, Player victim) {
        Location loc = victim.getLocation();
        Zombie soul = loc.getWorld().spawn(loc, Zombie.class, z -> {
            z.customName(Component.text(summoner.getName() + "의 영혼", NamedTextColor.DARK_GRAY));
            z.setCustomNameVisible(true);
            z.setBaby(false);
            z.setShouldBurnInDay(false);
            z.setCanPickupItems(false);
            z.setRemoveWhenFarAway(false);
            z.setGlowing(true);
            z.setPersistent(true);

            copyAttribute(victim, z, Attribute.MAX_HEALTH);
            copyAttribute(victim, z, Attribute.ATTACK_DAMAGE);
            copyAttribute(victim, z, Attribute.MOVEMENT_SPEED);

            AttributeInstance health = z.getAttribute(Attribute.MAX_HEALTH);
            if (health != null) z.setHealth(health.getValue());
        });
        Asurajang.getInstance().getScoreboardManager().addEntityToOwnerTeam(soul, summoner.getUniqueId());

        loc.getWorld().spawnParticle(Particle.SOUL, loc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_VEX_CHARGE, 1.0f, 0.6f);
        return soul;
    }

    private void copyAttribute(Player victim, Zombie soul, Attribute attribute) {
        AttributeInstance source = victim.getAttribute(attribute);
        AttributeInstance dest = soul.getAttribute(attribute);
        if (source != null && dest != null) dest.setBaseValue(source.getValue());
    }
}
