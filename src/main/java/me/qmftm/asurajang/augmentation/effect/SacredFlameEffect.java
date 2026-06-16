package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class SacredFlameEffect implements AugmentationEffect {

    private long lastUsed = 0;

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onRegainHealth(Player player, EntityRegainHealthEvent event) {
        long cooldownTicks = AugmentSettings.getLong("SacredFlame", "cooldown-ticks", 60L);
        long now = player.getWorld().getGameTime();
        if (now - lastUsed < (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(player))) return;

        double range = AugmentSettings.getDouble("SacredFlame", "range", 5.0);
        GameManager gm = Asurajang.getInstance().getGameManager();

        Player target = null;
        double minDistSq = range * range;
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player) || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (gm.getGameMode() == GameManager.GameMode.TEAM
                    && gm.getTeam(p.getUniqueId()) == gm.getTeam(player.getUniqueId())) continue;

            double distSq = p.getLocation().distanceSquared(player.getLocation());
            if (distSq < minDistSq) {
                minDistSq = distSq;
                target = p;
            }
        }

        if (target == null) return;

        int fireTicks = AugmentSettings.getInt("SacredFlame", "fire-ticks", 100);
        target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));
        lastUsed = now;
    }
}
