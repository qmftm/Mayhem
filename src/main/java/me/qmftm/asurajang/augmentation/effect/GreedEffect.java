package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GreedEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player attacker, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        GameScoreboardManager scoreboard = Asurajang.getInstance().getScoreboardManager();
        int targetGold = scoreboard.getGold(target);
        double ratio = AugmentSettings.getDouble("Greed", "steal-ratio", 0.01);
        int steal = (int)(targetGold * ratio);
        if (steal < 1) return;

        scoreboard.addGold(target, -steal);
        scoreboard.addGold(attacker, steal);

        attacker.sendActionBar(Component.text("+" + steal + " 골드!", NamedTextColor.GOLD));
    }
}
