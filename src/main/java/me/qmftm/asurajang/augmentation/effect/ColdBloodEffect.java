package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class ColdBloodEffect implements AugmentationEffect {

    private long lastUsed = 0;
    private BukkitTask cooldownNotifyTask;

    @Override public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        if (cooldownNotifyTask != null) { cooldownNotifyTask.cancel(); cooldownNotifyTask = null; }
    }

    @Override
    public void onInteractEntity(Player player, PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        if (target.equals(player)) return;

        long cooldownTicks = AugmentSettings.getLong("ColdBlood", "cooldown-ticks", 900L);

        long now = player.getWorld().getGameTime();
        if (now - lastUsed < cooldownTicks) {
            long remain = (cooldownTicks - (now - lastUsed) + 19) / 20;
            player.sendMessage(Component.text("[냉혈한] ", NamedTextColor.AQUA)
                    .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }

        int slownessDuration = AugmentSettings.getInt("ColdBlood", "slowness-duration", 60);
        int slownessAmplifier = AugmentSettings.getInt("ColdBlood", "slowness-amplifier", 3);
        int weaknessDuration = AugmentSettings.getInt("ColdBlood", "weakness-duration", 60);
        int weaknessAmplifier = AugmentSettings.getInt("ColdBlood", "weakness-amplifier", 0);

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, slownessAmplifier, false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, weaknessAmplifier, false, true));

        player.sendActionBar(Component.text("냉혈한 발동!", NamedTextColor.AQUA));
        player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 0.5f);

        lastUsed = now;
        Asurajang plugin = Asurajang.getInstance();
        if (cooldownNotifyTask != null) cooldownNotifyTask.cancel();
        cooldownNotifyTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendActionBar(Component.text("[냉혈한]", NamedTextColor.AQUA)
                        .append(Component.text("을 다시 사용 가능합니다", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }
            cooldownNotifyTask = null;
        }, cooldownTicks);
    }
}
