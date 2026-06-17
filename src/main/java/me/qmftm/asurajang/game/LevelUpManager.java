package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.gui.StatAnvilGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LevelUpManager {

    private static final Set<Integer> AUG_LEVELS   = Set.of(1, 3, 5, 7, 9, 11, 13, 15);
    private static final Set<Integer> PRISM_LEVELS  = Set.of(5, 10, 15);

    // 레벨업 시 자동으로 GUI를 열지 않을 주변 적 감지 범위 (반경, 블록)
    private static final double NEARBY_ENEMY_RANGE = 5.0;

    private final Map<UUID, Integer> augCharges   = new HashMap<>();
    private final Map<UUID, Integer> prismCharges = new HashMap<>();
    private final Map<UUID, Integer> anvilCharges = new HashMap<>();

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
        if (anvilCount > 0) anvilCharges.merge(uuid, anvilCount, Integer::sum);

        final int finalAugCount   = augCount;
        final int finalPrismCount = prismCount;
        final int finalAnvilCount = anvilCount;
        Asurajang.getInstance().getServer().getScheduler().runTaskLater(
            Asurajang.getInstance(), () -> {
                if (!player.isOnline()) return;
                player.showTitle(Title.title(
                    Component.empty(),
                    Component.text(newLevel + " Lv ", NamedTextColor.GOLD)
                        .append(Component.text("레벨 업!", NamedTextColor.YELLOW)),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(500))
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                if (finalAnvilCount > 0) {
                    player.sendMessage(Component.text(
                        "능력치 모루 +" + finalAnvilCount + "개 (남은 기회: " + getAnvilCharges(uuid) + ")",
                        NamedTextColor.DARK_PURPLE));
                }

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

                // 주변에 적이 없을 때만, 우선순위(프리즘 > 일반 증강 > 능력치 모루)에 따라 선택 GUI를 자동으로 열어줌
                if (!hasNearbyEnemy(player)) {
                    if (getPrismCharges(uuid) > 0) {
                        Asurajang.getInstance().openPrismAugmentationSelect(player);
                    } else if (getAugCharges(uuid) > 0) {
                        Asurajang.getInstance().openAugmentationSelect(player);
                    } else if (getAnvilCharges(uuid) > 0) {
                        new StatAnvilGUI().open(player);
                    }
                }
            }, 2L);
    }

    // 반경 NEARBY_ENEMY_RANGE 안에 적(다른 팀 또는 개인전 상대) 플레이어가 있는지 확인
    private boolean hasNearbyEnemy(Player player) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        boolean teamMode = gm.getGameMode() == GameManager.GameMode.TEAM;
        int myTeam = teamMode ? gm.getTeam(player.getUniqueId()) : -1;

        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player) || other.getGameMode() == GameMode.SPECTATOR) continue;
            if (other.getLocation().distance(player.getLocation()) > NEARBY_ENEMY_RANGE) continue;
            if (teamMode && gm.getTeam(other.getUniqueId()) == myTeam) continue;
            return true;
        }
        return false;
    }

    public int getAugCharges(UUID uuid)   { return augCharges.getOrDefault(uuid, 0); }
    public int getPrismCharges(UUID uuid) { return prismCharges.getOrDefault(uuid, 0); }
    public int getAnvilCharges(UUID uuid) { return anvilCharges.getOrDefault(uuid, 0); }

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

    public boolean consumeAnvilCharge(UUID uuid) {
        int c = anvilCharges.getOrDefault(uuid, 0);
        if (c <= 0) return false;
        anvilCharges.put(uuid, c - 1);
        return true;
    }

    public void grantAnvilCharges(UUID uuid, int count) {
        anvilCharges.merge(uuid, count, Integer::sum);
    }

    public void openNextGui(Player player) {
        UUID uuid = player.getUniqueId();
        Asurajang plugin = Asurajang.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (getPrismCharges(uuid) > 0) {
                plugin.openPrismAugmentationSelect(player);
            } else if (getAugCharges(uuid) > 0) {
                plugin.openAugmentationSelect(player);
            } else if (getAnvilCharges(uuid) > 0) {
                new StatAnvilGUI().open(player);
            }
        }, 1L);
    }

    public void cleanup(UUID uuid) {
        augCharges.remove(uuid);
        prismCharges.remove(uuid);
        anvilCharges.remove(uuid);
    }
}
