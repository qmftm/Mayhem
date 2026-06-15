package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

// 낙하 피해 면역(AugmentationEffectListener.onFallDamage에서 처리)
// + 공중에서 점프 키를 다시 누르면 1회 추가 점프(더블 점프) 가능
public class FeatherFallingEffect implements AugmentationEffect {

    private BukkitTask groundCheckTask;
    private boolean doubleJumpUsed = false;

    @Override
    public void onActivate(Player player) {
        doubleJumpUsed = false;
        player.setAllowFlight(true);

        long interval = AugmentSettings.getLong("FeatherFalling", "ground-check-interval-ticks", 5L);
        groundCheckTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (player.isOnline() && player.isOnGround()) doubleJumpUsed = false;
        }, 0L, interval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (groundCheckTask != null) { groundCheckTask.cancel(); groundCheckTask = null; }
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    @Override
    public void onToggleFlight(Player player, PlayerToggleFlightEvent event) {
        event.setCancelled(true);
        player.setFlying(false);

        if (doubleJumpUsed || player.isOnGround()) return;
        doubleJumpUsed = true;

        double power = AugmentSettings.getDouble("FeatherFalling", "double-jump-power", 0.5);
        Vector velocity = player.getVelocity();
        velocity.setY(power);
        player.setVelocity(velocity);

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 12, 0.3, 0.1, 0.3, 0.02);
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.3f);
    }
}
