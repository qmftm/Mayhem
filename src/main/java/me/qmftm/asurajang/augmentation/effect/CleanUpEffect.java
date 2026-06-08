package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class CleanUpEffect implements AugmentationEffect {

    private BukkitTask task;

    @Override
    public void onActivate(Player player) {
        NamespacedKey key = new NamespacedKey(Asurajang.getInstance(), "cleanup_speed");
        removeModifier(player, key);

        double range = AugmentSettings.getDouble("CleanUp", "range", 10.0);
        double healthThreshold = AugmentSettings.getDouble("CleanUp", "health-threshold", 0.3);
        double speedBonus = AugmentSettings.getDouble("CleanUp", "speed-bonus", 0.5);
        long checkInterval = AugmentSettings.getLong("CleanUp", "check-interval-ticks", 4L);

        task = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            if (!player.isOnline()) return;
            boolean trigger = false;
            for (Player p : player.getWorld().getPlayers()) {
                if (p.equals(player) || p.getGameMode() == GameMode.SPECTATOR) continue;
                if (p.getLocation().distance(player.getLocation()) > range) continue;
                if (p.getHealth() / p.getMaxHealth() <= healthThreshold) { trigger = true; break; }
            }

            AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (attr == null) return;

            boolean active = attr.getModifiers().stream()
                .anyMatch(m -> m.getKey().equals(key));

            if (trigger && !active) {
                attr.addModifier(new AttributeModifier(key, speedBonus, AttributeModifier.Operation.ADD_SCALAR));
            } else if (!trigger && active) {
                removeModifier(player, key);
            }
        }, 0L, checkInterval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (task != null) { task.cancel(); task = null; }
        removeModifier(player, new NamespacedKey(Asurajang.getInstance(), "cleanup_speed"));
    }

    private static void removeModifier(Player player, NamespacedKey key) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }
}
