package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class NaengnyeolhanEffect implements AugmentationEffect {

    private static final long COOLDOWN_MS = 45_000;

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

        long now = System.currentTimeMillis();
        if (now - lastUsed < COOLDOWN_MS) {
            long remain = (COOLDOWN_MS - (now - lastUsed) + 999) / 1000;
            player.sendMessage(Component.text("[냉혈한] ", NamedTextColor.AQUA)
                    .append(Component.text("쿨타임이 " + remain + "초 남았습니다.", NamedTextColor.GRAY)));
            return;
        }

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true));

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
        }, COOLDOWN_MS / 50);
    }
}
