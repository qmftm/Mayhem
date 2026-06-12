package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.SealManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SealEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        double radius = AugmentSettings.getDouble("Seal", "radius", 10.0);
        long durationTicks = AugmentSettings.getLong("Seal", "duration-ticks", 300L);

        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0),
                40, radius * 0.3, 1.0, radius * 0.3);
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);

        int count = 0;
        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) continue;
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distance(player.getLocation()) > radius) continue;

            SealManager.seal(target, durationTicks);
            target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4);
            target.playSound(target.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.8f);
            count++;
        }

        player.sendMessage(Component.text("[봉인] ", NamedTextColor.GRAY)
                .append(Component.text(count + "명에게 몰수 효과를 부여했습니다.", NamedTextColor.WHITE)));
    }

    @Override
    public void onDeactivate(Player player) {
    }
}
