package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class StarEffect implements AugmentationEffect {

    private BukkitTask revertTask;

    @Override
    public void onActivate(Player player) {
        long durationTicks = AugmentSettings.getLong("Star", "duration-ticks", 80L);

        player.setInvulnerable(true);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
            player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.5f);
        player.sendMessage(Component.text("[스타] ", NamedTextColor.GOLD)
            .append(Component.text("4초간 무적 상태가 됩니다!", NamedTextColor.YELLOW)));

        revertTask = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            revertTask = null;
            player.setInvulnerable(false);
        }, durationTicks);
    }

    @Override
    public void onDeactivate(Player player) {
        if (revertTask != null) { revertTask.cancel(); revertTask = null; }
        player.setInvulnerable(false);
    }
}
