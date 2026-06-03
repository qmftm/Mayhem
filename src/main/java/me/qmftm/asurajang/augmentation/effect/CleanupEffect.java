package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class CleanupEffect implements AugmentationEffect {

    private static final double RANGE           = 10.0;
    private static final double HEALTH_THRESH   = 0.3;  // 30% 이하

    private BukkitTask task;

    @Override
    public void onActivate(Player player) {
        task = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (!player.isOnline()) return;
            boolean trigger = false;
            for (Player p : player.getWorld().getPlayers()) {
                if (p.equals(player) || p.getGameMode() == GameMode.SPECTATOR) continue;
                if (p.getLocation().distance(player.getLocation()) > RANGE) continue;
                if (p.getHealth() / p.getMaxHealth() <= HEALTH_THRESH) { trigger = true; break; }
            }
            if (trigger) {
                // Speed V = 100% 이동속도 증가, 6틱 유지 (주기 4틱마다 갱신)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6, 4, false, false));
            }
        }, 0L, 4L);
    }

    @Override
    public void onDeactivate(Player player) {
        if (task != null) { task.cancel(); task = null; }
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
