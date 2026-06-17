package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.util.ActionBarTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class GamblerEffect implements AugmentationEffect {

    private static final int JACKPOT_NUMBER = 77;
    private static final long DISPLAY_BLOCK_MILLIS = 3000L;

    private BukkitTask task;

    @Override
    public void onActivate(Player player) {
        task = Asurajang.getInstance().getServer().getScheduler().runTaskTimer(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) return;

                int roll = ThreadLocalRandom.current().nextInt(1, 244);

                if (roll == JACKPOT_NUMBER) {
                    AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealth != null) {
                        double healRatio = AugmentSettings.getDouble("Gambler", "heal-ratio", 1.0);
                        double newHealth = Math.min(maxHealth.getValue(), player.getHealth() + maxHealth.getValue() * healRatio);
                        player.setHealth(newHealth);
                    }
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.2f);
                    org.bukkit.Location loc = player.getLocation();
                    for (int i = 0; i < 6; i++) {
                        player.getWorld().spawnParticle(Particle.NOTE,
                            loc.getX() + Math.random() * 0.6 - 0.3,
                            loc.getY() + 2,
                            loc.getZ() + Math.random() * 0.6 - 0.3,
                            0, 0, 0, 0, 6.0 / 24.0);
                    }
                }

                if (!ActionBarTracker.isRecentlyUsed(player, DISPLAY_BLOCK_MILLIS)) {
                    boolean jackpot = roll == JACKPOT_NUMBER;
                    player.sendActionBar(Component.text(String.valueOf(roll),
                            jackpot ? NamedTextColor.GOLD : NamedTextColor.YELLOW)
                        .decoration(TextDecoration.BOLD, jackpot)
                        .decoration(TextDecoration.ITALIC, false));
                }
            }, 0L, 1L);
    }

    @Override
    public void onDeactivate(Player player) {
        if (task != null) { task.cancel(); task = null; }
    }
}
