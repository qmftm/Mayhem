package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SelfDestructEffect implements AugmentationEffect {

    private BukkitTask bombTimer;
    private BukkitTask fuseTask;
    private BukkitTask tntFollowTask;
    private TNTPrimed  tntEntity;
    private boolean    pendingRespawnBomb;

    @Override
    public void onActivate(Player player) {
        scheduleBomb(player);
    }

    @Override
    public void onDeactivate(Player player) {
        if (bombTimer     != null) { bombTimer.cancel();     bombTimer     = null; }
        if (fuseTask      != null) { fuseTask.cancel();      fuseTask      = null; }
        if (tntFollowTask != null) { tntFollowTask.cancel(); tntFollowTask = null; }
        removeTnt();
        pendingRespawnBomb = false;
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void onOwnerDeath(Player player) {
        if (tntEntity != null) {
            // 퓨즈 중 사망 → 즉시 폭발
            if (fuseTask      != null) { fuseTask.cancel();      fuseTask      = null; }
            if (tntFollowTask != null) { tntFollowTask.cancel(); tntFollowTask = null; }
            player.removePotionEffect(PotionEffectType.SPEED);
            explode(player);
        } else {
            // 대기 중 사망 → 타이머 취소
            if (bombTimer != null) { bombTimer.cancel(); bombTimer = null; }
        }
        pendingRespawnBomb = true;
    }

    @Override
    public void onOwnerRespawn(Player player) {
        if (pendingRespawnBomb) {
            pendingRespawnBomb = false;
            scheduleBomb(player);
        }
    }

    private void scheduleBomb(Player player) {
        Asurajang plugin = Asurajang.getInstance();
        long bombInterval = AugmentSettings.getLong("SelfBomber", "bomb-interval-ticks", 700L);
        bombTimer = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !plugin.getGameManager().isRunning()) return;
            bombTimer = null;
            startFuse(player);
        }, bombInterval);
    }

    private void startFuse(Player player) {
        Asurajang plugin = Asurajang.getInstance();
        int fuseSeconds = AugmentSettings.getInt("SelfBomber", "fuse-seconds", 5);

        tntEntity = player.getWorld().spawn(player.getLocation().add(0, 2.5, 0), TNTPrimed.class, tnt -> {
            tnt.setFuseTicks(Integer.MAX_VALUE);
            tnt.setGravity(false);
            tnt.setInvulnerable(true);
            tnt.setYield(0);
        });
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, fuseSeconds * 20, 1, false, true));

        tntFollowTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (tntEntity != null && tntEntity.isValid()) {
                tntEntity.teleport(player.getLocation().add(0, 2.5, 0));
            }
        }, 0L, 1L);

        int[] fuse = {fuseSeconds};
        fuseTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !plugin.getGameManager().isRunning()) {
                if (fuseTask      != null) { fuseTask.cancel();      fuseTask      = null; }
                if (tntFollowTask != null) { tntFollowTask.cancel(); tntFollowTask = null; }
                removeTnt();
                return;
            }

            if (fuse[0] <= 0) {
                if (fuseTask      != null) { fuseTask.cancel();      fuseTask      = null; }
                if (tntFollowTask != null) { tntFollowTask.cancel(); tntFollowTask = null; }
                explode(player);
                scheduleBomb(player);
                return;
            }
            player.sendActionBar(Component.text(
                "폭발까지 " + fuse[0] + "초!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f,
                0.5f + (fuseSeconds - fuse[0]) * 0.1f);
            fuse[0]--;
        }, 0L, 20L);
    }

    private void removeTnt() {
        if (tntEntity != null) {
            tntEntity.remove();
            tntEntity = null;
        }
    }

    private void explode(Player player) {
        removeTnt();
        Location loc = player.getLocation().add(0, 1, 0);

        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 12, 1.2, 0.6, 1.2, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

        double radius = AugmentSettings.getDouble("SelfBomber", "radius", 8.0);
        double damageFraction = AugmentSettings.getDouble("SelfBomber", "damage-fraction", 0.2);
        double selfDamageMultiplier = AugmentSettings.getDouble("SelfBomber", "self-damage-multiplier", 0.5);
        double knockbackHorizontal = AugmentSettings.getDouble("SelfBomber", "knockback-horizontal", 1.5);
        double knockbackVertical = AugmentSettings.getDouble("SelfBomber", "knockback-vertical", 0.4);

        for (Player target : player.getWorld().getPlayers()) {
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distance(loc) > radius) continue;

            boolean isSelf = target.equals(player);
            double damage = target.getMaxHealth() * damageFraction * (isSelf ? selfDamageMultiplier : 1.0);
            target.damage(damage, player);

            if (isSelf) continue;

            Vector dir = target.getLocation().subtract(loc).toVector().setY(0);
            if (dir.lengthSquared() > 0.001) dir.normalize();
            target.setVelocity(dir.multiply(knockbackHorizontal).setY(knockbackVertical));
        }
    }
}
