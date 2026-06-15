package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class BodyguardEffect implements AugmentationEffect, Listener {

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

        GameManager gm = Asurajang.getInstance().getGameManager();
        if (gm.getGameMode() != GameManager.GameMode.TEAM) return;

        double radius = AugmentSettings.getDouble("Bodyguard", "radius", 8.0);
        double reductionPerAlly = AugmentSettings.getDouble("Bodyguard", "reduction-per-ally", 0.05);
        int team = gm.getTeam(player.getUniqueId());

        long allyCount = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player) && p.getGameMode() != GameMode.SPECTATOR)
            .filter(p -> gm.getTeam(p.getUniqueId()) == team)
            .filter(p -> p.getLocation().distance(player.getLocation()) <= radius)
            .count();

        if (allyCount == 0) return;

        double factor = Math.max(0.0, 1 - allyCount * reductionPerAlly);
        event.setDamage(event.getDamage() * factor);
    }
}
