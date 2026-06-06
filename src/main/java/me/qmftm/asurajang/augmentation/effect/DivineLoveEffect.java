package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class DivineLoveEffect implements AugmentationEffect {

    private static final long COOLDOWN_MS = 25_000;
    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onRightClick(Player player, PlayerInteractEvent event) {
        if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD")) return;

        long now = System.currentTimeMillis();
        if (now - lastUsed < COOLDOWN_MS) return;
        lastUsed = now;

        event.setCancelled(true);

        Asurajang plugin = Asurajang.getInstance();

        // 3발을 15틱(0.75초) 간격으로 발사
        for (int i = 0; i < 3; i++) {
            final long delay = i * 15L;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline() || !plugin.getGameManager().isRunning()) return;
                Location eye = player.getEyeLocation();
                Vector dir = eye.getDirection().normalize();
                player.getWorld().spawn(eye.clone().add(dir), SmallFireball.class, fb -> {
                    fb.setShooter(player);
                    fb.setDirection(dir.clone().multiply(1.5));
                });
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);
            }, delay);
        }

        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[주님의 사랑]", NamedTextColor.GOLD)
                    .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, COOLDOWN_MS / 50);
    }
}
