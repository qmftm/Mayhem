package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.event.PlayerExpRewardEvent;
import me.qmftm.asurajang.event.PlayerGoldRewardEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RewardMessageListener implements Listener {

    private final Map<UUID, Pending> pending   = new HashMap<>();
    private final Set<UUID>          scheduled = new HashSet<>();

    @EventHandler
    public void onGoldReward(PlayerGoldRewardEvent event) {
        Pending p = pending.computeIfAbsent(event.getPlayer().getUniqueId(), k -> new Pending());
        p.gold          = event.getAmount();
        p.multiKillLabel = event.getMultiKillLabel();
        p.goldReasons   = new ArrayList<>(event.getBonusReasons());
        scheduleFlush(event.getPlayer());
    }

    @EventHandler
    public void onExpReward(PlayerExpRewardEvent event) {
        Pending p = pending.computeIfAbsent(event.getPlayer().getUniqueId(), k -> new Pending());
        p.exp       = event.getAmount();
        p.leveledUp = event.isLeveledUp();
        p.newLevel  = event.getNewLevel();
        scheduleFlush(event.getPlayer());
    }

    private void scheduleFlush(Player player) {
        UUID uuid = player.getUniqueId();
        if (scheduled.add(uuid)) {
            Asurajang.getInstance().getServer().getScheduler()
                .runTaskLater(Asurajang.getInstance(), () -> flush(player), 1L);
        }
    }

    private void flush(Player player) {
        UUID uuid = player.getUniqueId();
        scheduled.remove(uuid);
        Pending p = pending.remove(uuid);
        if (p == null || !player.isOnline()) return;

        Component.Builder b = Component.text();

        if (p.gold > 0) {
            b.append(p.multiKillLabel);
            b.append(Component.text("+" + p.gold + " 골드", NamedTextColor.GOLD));
            if (!p.goldReasons.isEmpty()) {
                b.append(Component.text(" (" + String.join(", ", p.goldReasons) + ")", NamedTextColor.GRAY));
            }
        }

        if (p.exp > 0) {
            if (p.gold > 0) b.append(Component.text("  ", NamedTextColor.WHITE));
            if (p.leveledUp) {
                b.append(Component.text("레벨 업 " + p.newLevel + " Lv  ", NamedTextColor.GREEN));
            }
            b.append(Component.text("+" + p.exp + " Exp", NamedTextColor.DARK_GREEN));
        }

        player.sendMessage(b.build());
    }

    private static class Pending {
        int gold = 0;
        int exp  = 0;
        Component multiKillLabel = Component.empty();
        List<String> goldReasons = new ArrayList<>();
        boolean leveledUp = false;
        int newLevel = 1;
    }
}
