package me.qmftm.asurajang.augmentation.effect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NaengnyeolhanEffect implements AugmentationEffect {

    private static final long COOLDOWN_MS = 5_000;
    private static final int  RANGE       = 5;

    private long lastUsed = 0;

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onRightClick(Player player, PlayerInteractEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastUsed < COOLDOWN_MS) return;

        Entity targeted = player.getTargetEntity(RANGE, false);
        if (!(targeted instanceof Player target) || target.equals(player)) return;

        // 구속 III (Slowness, amplifier 2 = level III), 3초
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, true));
        // 나약함 I (Weakness, amplifier 0 = level I), 3초
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true));

        player.sendActionBar(Component.text("냉혈한 발동!", NamedTextColor.AQUA));
        player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 0.5f);

        lastUsed = now;
    }
}
