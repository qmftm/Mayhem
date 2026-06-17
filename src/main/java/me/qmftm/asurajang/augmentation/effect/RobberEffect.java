package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class RobberEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player attacker, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        double chance = AugmentSettings.getDouble("Robber", "steal-chance", 0.5);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        int stealAmount = AugmentSettings.getInt("Robber", "steal-amount", 5);
        GameScoreboardManager scoreboard = Asurajang.getInstance().getScoreboardManager();

        int targetGold = scoreboard.getGold(target);
        int actual = Math.min(stealAmount, targetGold);
        if (actual <= 0) return;

        scoreboard.addGold(target, -actual);
        scoreboard.addGold(attacker, actual);

        attacker.sendActionBar(Component.text("+" + actual + " 골드!", NamedTextColor.GOLD));
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.6f);
        target.getWorld().spawnParticle(Particle.WAX_OFF, target.getLocation().add(0, 1, 0),
            5, 0.3, 0.3, 0.3, 0);
    }
}
