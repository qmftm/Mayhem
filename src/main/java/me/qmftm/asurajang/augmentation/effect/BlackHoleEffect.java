package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class BlackHoleEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onToggleSneak(Player player, PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        long cooldownTicks = AugmentSettings.getLong("BlackHole", "cooldown-ticks", 700L);
        long effectiveCooldown = (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(player));
        long now = player.getWorld().getGameTime();
        if (now - lastUsed < effectiveCooldown) return;
        lastUsed = now;

        double radius = AugmentSettings.getDouble("BlackHole", "radius", 4.0);
        int slownessDuration = AugmentSettings.getInt("BlackHole", "slowness-duration-ticks", 80);
        int slownessAmplifier = AugmentSettings.getInt("BlackHole", "slowness-amplifier", 3);
        double damageTotal = AugmentSettings.getDouble("BlackHole", "damage-total", 4.0);
        int damageTicks = AugmentSettings.getInt("BlackHole", "damage-ticks", 4);
        long damageInterval = AugmentSettings.getLong("BlackHole", "damage-interval-ticks", 20L);

        GameManager gm = Asurajang.getInstance().getGameManager();
        Location loc = player.getLocation();
        double radiusSq = radius * radius;

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player) || target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distanceSquared(loc) > radiusSq) continue;
            if (gm.getGameMode() == GameManager.GameMode.TEAM
                    && gm.getTeam(target.getUniqueId()) == gm.getTeam(player.getUniqueId())) continue;

            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, slownessAmplifier, false, true));
            applyFallDamage(target, damageTotal / damageTicks, damageTicks, damageInterval);
        }

        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 80, radius / 2, 1.0, radius / 2, 0.15);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);

        player.sendActionBar(Component.text("블랙홀 발동!", NamedTextColor.DARK_PURPLE));
        ActionBarTracker.markUsed(player);

        Asurajang plugin = Asurajang.getInstance();
        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[블랙홀]", NamedTextColor.DARK_PURPLE)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, effectiveCooldown);
    }

    private void applyFallDamage(Player target, double perTick, int ticks, long interval) {
        Asurajang plugin = Asurajang.getInstance();
        int[] remaining = {ticks};
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!target.isOnline() || target.isDead()) {
                holder[0].cancel();
                return;
            }
            target.setHealth(Math.max(0.0, target.getHealth() - perTick));
            remaining[0]--;
            if (remaining[0] <= 0) holder[0].cancel();
        }, interval, interval);
    }
}
