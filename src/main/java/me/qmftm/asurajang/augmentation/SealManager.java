package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.util.ActionBarCountdown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "몰수" 상태를 관리한다. 몰수 상태인 동안 모든 증강 사용이 금지되고
 * 공격력이 감소한다.
 */
public final class SealManager {

    private static final Map<UUID, BukkitTask> sealTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> displayTasks = new ConcurrentHashMap<>();

    private SealManager() {
    }

    public static void seal(Player player, long durationTicks) {
        UUID id = player.getUniqueId();
        Asurajang plugin = Asurajang.getInstance();

        BukkitTask prevSeal = sealTasks.remove(id);
        if (prevSeal != null) prevSeal.cancel();
        BukkitTask prevDisplay = displayTasks.remove(id);
        if (prevDisplay != null) prevDisplay.cancel();

        player.sendMessage(Component.text("[봉인] ", NamedTextColor.GRAY)
                .append(Component.text("몰수 상태가 되었습니다! 증강을 사용할 수 없고 공격력이 감소합니다.", NamedTextColor.WHITE)));

        long[] remainingTicks = {durationTicks};
        BukkitTask displayTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            ActionBarCountdown.show(player, "몰수", NamedTextColor.GRAY, remainingTicks[0]);
            remainingTicks[0]--;
        }, 0L, 1L);
        displayTasks.put(id, displayTask);

        BukkitTask sealTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sealTasks.remove(id);
            BukkitTask dt = displayTasks.remove(id);
            if (dt != null) dt.cancel();
            if (player.isOnline()) {
                player.sendMessage(Component.text("[봉인] ", NamedTextColor.GRAY)
                        .append(Component.text("몰수 상태가 해제되었습니다.", NamedTextColor.GREEN)));
            }
        }, durationTicks);
        sealTasks.put(id, sealTask);
    }

    public static boolean isSealed(UUID playerId) {
        return sealTasks.containsKey(playerId);
    }

    public static void clear(UUID playerId) {
        BukkitTask sealTask = sealTasks.remove(playerId);
        if (sealTask != null) sealTask.cancel();
        BukkitTask displayTask = displayTasks.remove(playerId);
        if (displayTask != null) displayTask.cancel();
    }
}
