package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CorrosionEffect implements AugmentationEffect {

    private long lastUsed = 0;

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        long cooldownTicks = AugmentSettings.getLong("Corrosion", "cooldown-ticks", 200L);
        long now = player.getWorld().getGameTime();
        if (now - lastUsed < (long)(cooldownTicks * AugmentSettings.getCooldownMultiplier(player))) return;

        int duration = AugmentSettings.getInt("Corrosion", "wither-duration", 100);
        int amplifier = AugmentSettings.getInt("Corrosion", "wither-amplifier", 0);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, amplifier, false, true));

        lastUsed = now;
    }
}
