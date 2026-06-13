package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class StatEffect implements AugmentationEffect {

    private static final int ANVIL_COUNT = 2;

    @Override
    public void onActivate(Player player) {
        var levelUpManager = Asurajang.getInstance().getLevelUpManager();
        levelUpManager.grantAnvilCharges(player.getUniqueId(), ANVIL_COUNT);
        player.sendMessage(Component.text(
            "능력치 모루 +" + ANVIL_COUNT + "개 (남은 기회: " + levelUpManager.getAnvilCharges(player.getUniqueId()) + ")",
            NamedTextColor.DARK_PURPLE));
    }

    @Override
    public void onDeactivate(Player player) {}
}
