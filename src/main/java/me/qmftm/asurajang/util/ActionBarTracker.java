package me.qmftm.asurajang.util;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 다른 시스템이 액션바를 표시했는지 추적하는 유틸리티.
 * 매 틱 갱신되는 액션바(예: 도박사)가 다른 중요한 액션바 메시지를 가리지 않도록 사용한다.
 */
public final class ActionBarTracker {

    private static final Map<UUID, Long> lastUsed = new ConcurrentHashMap<>();

    private ActionBarTracker() {
    }

    public static void markUsed(Player player) {
        lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static boolean isRecentlyUsed(Player player, long withinMillis) {
        Long last = lastUsed.get(player.getUniqueId());
        return last != null && System.currentTimeMillis() - last < withinMillis;
    }
}
