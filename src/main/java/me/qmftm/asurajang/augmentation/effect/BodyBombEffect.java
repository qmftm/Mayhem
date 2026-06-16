package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BodyBombEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onDropItem(Player player, PlayerDropItemEvent event) {
        event.setCancelled(true);

        long cooldownTicks = AugmentSettings.getLong("BodyBomb", "cooldown-ticks", 1000L);
        long effectiveCooldown = (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(player));
        double healthCost = AugmentSettings.getDouble("BodyBomb", "health-cost", 6.0);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < effectiveCooldown) {
            long remain = (effectiveCooldown - (now - lastUsed) + 19) / 20;
            player.sendMessage(Component.text("[신체 폭탄] ", NamedTextColor.DARK_GREEN)
                    .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }

        if (player.getHealth() <= healthCost) {
            player.sendMessage(Component.text("[신체 폭탄] ", NamedTextColor.DARK_GREEN)
                    .append(Component.text("체력이 부족합니다.", NamedTextColor.GRAY)));
            return;
        }
        lastUsed = now;

        player.setHealth(player.getHealth() - healthCost);

        int fuseTicks = AugmentSettings.getInt("BodyBomb", "fuse-ticks", 40);
        double throwVelocity = AugmentSettings.getDouble("BodyBomb", "throw-velocity", 1.3);

        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        TNTPrimed tnt = player.getWorld().spawn(eye.clone().add(dir), TNTPrimed.class, t -> {
            t.setFuseTicks(Integer.MAX_VALUE);
            t.setInvulnerable(true);
            t.setVelocity(dir.clone().multiply(throwVelocity));
        });

        player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
        player.sendActionBar(Component.text("신체 폭탄!", NamedTextColor.DARK_GREEN));
        ActionBarTracker.markUsed(player);

        Asurajang plugin = Asurajang.getInstance();
        Bukkit.getScheduler().runTaskLater(plugin, () -> explode(player, tnt), fuseTicks);

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[신체 폭탄]", NamedTextColor.DARK_GREEN)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                ActionBarTracker.markUsed(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, effectiveCooldown);
    }

    private void explode(Player player, TNTPrimed tnt) {
        Location loc = tnt.isValid() ? tnt.getLocation() : player.getLocation();
        if (tnt.isValid()) tnt.remove();

        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 12, 1.2, 0.6, 1.2, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

        double radius = AugmentSettings.getDouble("BodyBomb", "radius", 4.0);
        double damageFraction = AugmentSettings.getDouble("BodyBomb", "damage-fraction", 0.35);
        double selfDamageMultiplier = AugmentSettings.getDouble("BodyBomb", "self-damage-multiplier", 0.5);
        double knockbackHorizontal = AugmentSettings.getDouble("BodyBomb", "knockback-horizontal", 1.2);
        double knockbackVertical = AugmentSettings.getDouble("BodyBomb", "knockback-vertical", 0.4);

        for (Player target : loc.getWorld().getPlayers()) {
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distance(loc) > radius) continue;

            boolean isSelf = target.equals(player);
            double damage = target.getMaxHealth() * damageFraction * (isSelf ? selfDamageMultiplier : 1.0);
            target.damage(damage, player);

            Vector kb = target.getLocation().subtract(loc).toVector().setY(0);
            if (kb.lengthSquared() > 0.001) kb.normalize();
            target.setVelocity(target.getVelocity().add(kb.multiply(knockbackHorizontal)).setY(knockbackVertical));
        }
    }
}
