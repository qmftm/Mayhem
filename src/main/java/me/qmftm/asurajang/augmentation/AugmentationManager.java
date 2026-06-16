package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AugmentationManager {

    private final Map<String, Augmentation> augmentations = new LinkedHashMap<>();
    private final Set<String> synergyIds = new HashSet<>();
    private final Map<UUID, Map<String, AugmentationEffect>> playerEffects = new HashMap<>();

    public AugmentationManager(FileConfiguration regular, FileConfiguration prism) {
        load(regular, false);
        load(prism, true);
    }

    public void reload(FileConfiguration regular, FileConfiguration prism) {
        augmentations.clear();
        synergyIds.clear();
        load(regular, false);
        load(prism, true);
    }

    private void load(FileConfiguration config, boolean prism) {
        for (String id : config.getKeys(false)) {
            ConfigurationSection entry = config.getConfigurationSection(id);
            if (entry == null) continue;

            String displayName = entry.getString("display-name", id);

            String iconStr = entry.getString("icon", "NETHER_STAR");
            Material iconMat = Material.matchMaterial(iconStr);
            if (iconMat == null) iconMat = Material.NETHER_STAR;

            String colorStr = entry.getString("color", "white").toLowerCase();
            NamedTextColor color = NamedTextColor.NAMES.value(colorStr);
            if (color == null) color = NamedTextColor.WHITE;

            List<String> descLines = entry.getStringList("description");
            boolean active = entry.getBoolean("active", false);
            int cooldown = entry.getInt("cooldown", 30);
            boolean cooldownOnUse = entry.getBoolean("cooldown-on-use", true);

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

            augmentations.put(id, new Augmentation(id, displayName, icon, prism, active, cooldown, cooldownOnUse));
        }
    }

    // ── 시너지 등록 ──────────────────────────────────────────────────────────

    public void addSynergy(Augmentation aug) {
        augmentations.put(aug.getId(), aug);
        synergyIds.add(aug.getId());
    }

    // ── 효과 활성화 / 비활성화 ───────────────────────────────────────────────

    public void activateFor(Player player, String augId) {
        activateInternal(player, augId);
        Asurajang.getInstance().getSynergyManager().checkAndApply(player, this);
    }

    void activateInternal(Player player, String augId) {
        AugmentationEffect effect = AugmentationRegistry.create(augId);
        if (effect == null) return;

        playerEffects.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                     .put(augId, effect);
        effect.onActivate(player);
        Asurajang.getInstance().getMaxHealthManager().recalculate(player);
    }

    public void deactivateFor(Player player) {
        Asurajang.getInstance().getSynergyManager().clearPlayer(player.getUniqueId());
        Map<String, AugmentationEffect> effects = playerEffects.remove(player.getUniqueId());
        if (effects == null) return;
        effects.values().forEach(effect -> effect.onDeactivate(player));
        if (player.isOnline()) Asurajang.getInstance().getMaxHealthManager().recalculate(player);
    }

    public void deactivateSingle(Player player, String augId) {
        Map<String, AugmentationEffect> effects = playerEffects.get(player.getUniqueId());
        if (effects == null) return;
        AugmentationEffect effect = effects.remove(augId);
        if (effect != null) effect.onDeactivate(player);
        Asurajang.getInstance().getMaxHealthManager().recalculate(player);
    }

    public void deactivateAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            deactivateFor(player);
        }
    }

    public Map<String, AugmentationEffect> getActiveEffects(UUID playerId) {
        return playerEffects.getOrDefault(playerId, Collections.emptyMap());
    }

    // ── 조회 ────────────────────────────────────────────────────────────────

    public Augmentation get(String id) {
        return augmentations.get(id);
    }

    public List<Augmentation> getAll() {
        return augmentations.values().stream()
                .filter(a -> !a.isPrism() && !synergyIds.contains(a.getId()))
                .toList();
    }

    public List<Augmentation> getPrismAll() {
        return augmentations.values().stream()
                .filter(a -> a.isPrism() && !synergyIds.contains(a.getId()))
                .toList();
    }
}
