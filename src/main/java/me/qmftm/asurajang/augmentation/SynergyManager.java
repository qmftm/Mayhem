package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SynergyManager {

    private record SynergyDef(String id, List<String> components, Augmentation augmentation) {}

    private final List<SynergyDef> synergies = new ArrayList<>();

    public SynergyManager(FileConfiguration config) {
        load(config);
    }

    public void reload(FileConfiguration config, AugmentationManager augManager) {
        load(config);
        for (SynergyDef def : synergies) {
            augManager.addSynergy(def.augmentation());
        }
    }

    private void load(FileConfiguration config) {
        synergies.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection entry = config.getConfigurationSection(id);
            if (entry == null) continue;

            List<String> components = entry.getStringList("components");
            if (components.size() < 2) continue;

            String displayName = entry.getString("display-name", id);
            String iconStr = entry.getString("icon", "NETHER_STAR");
            Material iconMat = Material.matchMaterial(iconStr);
            if (iconMat == null) iconMat = Material.NETHER_STAR;

            String colorStr = entry.getString("color", "gold").toLowerCase();
            NamedTextColor color = NamedTextColor.NAMES.value(colorStr);
            if (color == null) color = NamedTextColor.GOLD;

            List<String> descLines = entry.getStringList("description");

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

            synergies.add(new SynergyDef(id, List.copyOf(components),
                    new Augmentation(id, displayName, icon, false, false, 0, false)));
        }
    }

    public List<Augmentation> getSynergyAugmentations() {
        return synergies.stream().map(SynergyDef::augmentation).toList();
    }

    public void checkAndApply(Player player, AugmentationManager augManager) {
        Set<String> active = new HashSet<>(augManager.getActiveEffects(player.getUniqueId()).keySet());

        List<SynergyDef> triggered = new ArrayList<>();
        Set<String> consumed = new HashSet<>();

        for (SynergyDef def : synergies) {
            if (active.contains(def.id())) continue;
            if (!active.containsAll(def.components())) continue;
            if (def.components().stream().anyMatch(consumed::contains)) continue;
            triggered.add(def);
            consumed.addAll(def.components());
        }

        for (SynergyDef def : triggered) {
            for (String comp : def.components()) {
                augManager.deactivateSingle(player, comp);
            }
            augManager.activateInternal(player, def.id());
            notifyPlayer(player, def);
        }
    }

    private void notifyPlayer(Player player, SynergyDef def) {
        Augmentation aug = def.augmentation();
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
