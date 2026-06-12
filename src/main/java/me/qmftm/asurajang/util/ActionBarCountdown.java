package me.qmftm.asurajang.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

/**
 * "이름: 2.75초" 형식의 카운트다운 액션바를 표시하는 유틸리티.
 */
public final class ActionBarCountdown {

    private ActionBarCountdown() {
    }

    public static void show(Player player, String label, TextColor color, long remainingTicks) {
        double seconds = remainingTicks / 20.0;
        player.sendActionBar(Component.text(label + ": " + String.format("%.2f초", seconds), color));
        ActionBarTracker.markUsed(player);
    }
}
