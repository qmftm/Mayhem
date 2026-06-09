package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LevelUpManager {

    private static final Set<Integer> AUG_LEVELS   = Set.of(3, 6, 9, 12, 15);
    private static final Set<Integer> PRISM_LEVELS  = Set.of(5, 10, 15);

    private final Map<UUID, Integer> augCharges   = new HashMap<>();
    private final Map<UUID, Integer> prismCharges = new HashMap<>();

    public void onLevelUp(Player player, int oldLevel, int newLevel) {
        int augCount   = 0;
        int prismCount = 0;
        int anvilCount = newLevel - oldLevel;

        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
            if (AUG_LEVELS.contains(lvl))  augCount++;
            if (PRISM_LEVELS.contains(lvl)) prismCount++;
        }

        UUID uuid = player.getUniqueId();
        if (augCount > 0)   augCharges.merge(uuid, augCount, Integer::sum);
        if (prismCount > 0) prismCharges.merge(uuid, prismCount, Integer::sum);

        for (int i = 0; i < anvilCount; i++) {
            player.getInventory().addItem(Asurajang.createStatAnvilItem());
        }

        final int finalAugCount   = augCount;
        final int finalPrismCount = prismCount;
        final int finalAnvilCount = anvilCount;
        Asurajang.getInstance().getServer().getScheduler().runTaskLater(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) return;
                player.showTitle(Title.title(
                    Component.text(newLevel + " Lv", NamedTextColor.GOLD),
                    Component.text("레벨 업!", NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(500))
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                player.sendMessage(Component.text(
                    "능력치 모루 +" + finalAnvilCount + "개 (인벤토리 확인)",
                    NamedTextColor.DARK_PURPLE));

                if (finalAugCount > 0) {
                    player.sendMessage(Component.text(
                        "증강 선택 기회 +" + finalAugCount + "회 (남은 기회: " + getAugCharges(uuid) + ")",
                        NamedTextColor.LIGHT_PURPLE));
                }
                if (finalPrismCount > 0) {
                    player.sendMessage(Component.text(
                        "프리즘 증강 선택 기회 +" + finalPrismCount + "회 (남은 기회: " + getPrismCharges(uuid) + ")",
                        NamedTextColor.AQUA));
                }
            }, 2L);
    }

    public int getAugCharges(UUID uuid)   { return augCharges.getOrDefault(uuid, 0); }
    public int getPrismCharges(UUID uuid) { return prismCharges.getOrDefault(uuid, 0); }

    public boolean consumeAugCharge(UUID uuid) {
        int c = augCharges.getOrDefault(uuid, 0);
        if (c <= 0) return false;
        augCharges.put(uuid, c - 1);
        return true;
    }

    public boolean consumePrismCharge(UUID uuid) {
        int c = prismCharges.getOrDefault(uuid, 0);
        if (c <= 0) return false;
        prismCharges.put(uuid, c - 1);
        return true;
    }

    public void cleanup(UUID uuid) {
        augCharges.remove(uuid);
        prismCharges.remove(uuid);
    }
}
