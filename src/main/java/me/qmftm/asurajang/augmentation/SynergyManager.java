package me.qmftm.asurajang.augmentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SynergyManager {

    // 한 그룹 안의 단계 하나 (effectId = AugmentationRegistry 키)
    private record TierDef(String effectId, List<String> components, Augmentation augmentation) {}

    // 같은 증강 조합의 단계 묶음 (tiers는 컴포넌트 수 내림차순 정렬)
    private record GroupDef(String groupId, List<TierDef> tiers) {}

    private final List<GroupDef> groups = new ArrayList<>();

    // 플레이어별 그룹별 현재 활성 단계 effectId
    private final Map<UUID, Map<String, String>> activeTiers = new HashMap<>();

    public SynergyManager(FileConfiguration config) {
        load(config);
    }

    public void reload(FileConfiguration config, AugmentationManager augManager) {
        groups.clear();
        load(config);
        getSynergyAugmentations().forEach(augManager::addSynergy);
    }

    private void load(FileConfiguration config) {
        for (String groupId : config.getKeys(false)) {
            ConfigurationSection groupSec = config.getConfigurationSection(groupId);
            if (groupSec == null) continue;

            String iconStr = groupSec.getString("icon", "NETHER_STAR");
            Material iconMat = Material.matchMaterial(iconStr);
            if (iconMat == null) iconMat = Material.NETHER_STAR;

            String colorStr = groupSec.getString("color", "gold").toLowerCase();
            NamedTextColor color = NamedTextColor.NAMES.value(colorStr);
            if (color == null) color = NamedTextColor.GOLD;

            List<TierDef> tiers = new ArrayList<>();

            ConfigurationSection tiersSec = groupSec.getConfigurationSection("tiers");
            if (tiersSec != null) {
                // 다단계 형식: tiers 아래 각 키가 effectId
                for (String tierId : tiersSec.getKeys(false)) {
                    ConfigurationSection tierSec = tiersSec.getConfigurationSection(tierId);
                    if (tierSec == null) continue;
                    List<String> components = tierSec.getStringList("components");
                    if (components.isEmpty()) continue;
                    Augmentation aug = buildAugmentation(tierId, tierSec, iconMat, color);
                    tiers.add(new TierDef(tierId, List.copyOf(components), aug));
                }
            } else {
                // 단일 단계 형식 (기존 호환)
                List<String> components = groupSec.getStringList("components");
                if (components.isEmpty()) continue;
                Augmentation aug = buildAugmentation(groupId, groupSec, iconMat, color);
                tiers.add(new TierDef(groupId, List.copyOf(components), aug));
            }

            // 컴포넌트 수 많은 단계부터 먼저 검사
            tiers.sort((a, b) -> b.components().size() - a.components().size());
            groups.add(new GroupDef(groupId, tiers));
        }
    }

    private Augmentation buildAugmentation(String id, ConfigurationSection sec,
                                           Material iconMat, NamedTextColor color) {
        String displayName = sec.getString("display-name", id);

        // 개별 단계에서 아이콘/색상 재정의 허용
        String localIcon = sec.getString("icon");
        if (localIcon != null) {
            Material m = Material.matchMaterial(localIcon);
            if (m != null) iconMat = m;
        }
        String localColor = sec.getString("color");
        if (localColor != null) {
            NamedTextColor c = NamedTextColor.NAMES.value(localColor.toLowerCase());
            if (c != null) color = c;
        }

        List<String> descLines = sec.getStringList("description");

        ItemStack icon = new ItemStack(iconMat);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Component.text(displayName, color).decoration(TextDecoration.ITALIC, false));
        if (!descLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : descLines) {
                lore.add(Component.empty().color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(MiniMessage.miniMessage().deserialize(line)));
            }
            meta.lore(lore);
        }
        icon.setItemMeta(meta);

        return new Augmentation(id, displayName, icon, false, false, 0, false);
    }

    public List<Augmentation> getSynergyAugmentations() {
        return groups.stream()
                .flatMap(g -> g.tiers().stream())
                .map(TierDef::augmentation)
                .toList();
    }

    public void checkAndApply(Player player, AugmentationManager augManager) {
        Set<String> active = new HashSet<>(augManager.getActiveEffects(player.getUniqueId()).keySet());
        Map<String, String> playerTiers = activeTiers.computeIfAbsent(
                player.getUniqueId(), k -> new HashMap<>());

        for (GroupDef group : groups) {
            String currentId = playerTiers.get(group.groupId());

            // 가장 높은 조건을 만족하는 단계 탐색
            TierDef qualifying = null;
            for (TierDef tier : group.tiers()) {
                if (active.containsAll(tier.components())) {
                    qualifying = tier;
                    break;
                }
            }

            String qualifyingId = qualifying != null ? qualifying.effectId() : null;
            if (Objects.equals(currentId, qualifyingId)) continue;

            // 이전 단계 효과 해제
            if (currentId != null) {
                augManager.deactivateSingle(player, currentId);
                playerTiers.remove(group.groupId());
            }

            // 새 단계 효과 추가 (구성 증강은 건드리지 않음)
            if (qualifying != null) {
                augManager.activateInternal(player, qualifyingId);
                notifyPlayer(player, qualifying);
                playerTiers.put(group.groupId(), qualifyingId);
            }
        }
    }

    public void clearPlayer(UUID playerId) {
        activeTiers.remove(playerId);
    }

    private void notifyPlayer(Player player, TierDef tier) {
        Augmentation aug = tier.augmentation();
        ItemMeta meta = aug.getIcon().getItemMeta();
        Component name = meta.displayName();

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.sendMessage(Component.text("✦ 시너지 발동!  ", NamedTextColor.GOLD)
                .append(name != null ? name : Component.text(aug.getDisplayName(), NamedTextColor.GOLD)));
        if (meta.hasLore()) {
            for (Component line : meta.lore()) {
                player.sendMessage(line);
            }
        }
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
}
