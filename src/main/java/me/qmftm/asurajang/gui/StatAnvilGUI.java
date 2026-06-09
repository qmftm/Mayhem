package me.qmftm.asurajang.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StatAnvilGUI implements InventoryHolder {

    public enum Stat {
        ATTACK    ("공격력",   Material.IRON_SWORD,      "+2 공격력",      NamedTextColor.RED),
        DEFENSE   ("방어력",   Material.IRON_CHESTPLATE, "+1 방어력",      NamedTextColor.BLUE),
        MAX_HEALTH("최대 체력", Material.GOLDEN_APPLE,   "+4 최대 체력",   NamedTextColor.GREEN),
        SPEED     ("이동속도", Material.FEATHER,          "+0.01 이동속도", NamedTextColor.AQUA);

        private final String displayName;
        private final Material icon;
        private final String description;
        private final NamedTextColor color;

        Stat(String displayName, Material icon, String description, NamedTextColor color) {
            this.displayName = displayName;
            this.icon        = icon;
            this.description = description;
            this.color       = color;
        }

        public String getDisplayName()  { return displayName; }
        public Material getIcon()       { return icon; }
        public String getDescription()  { return description; }
        public NamedTextColor getColor(){ return color; }
    }

    private static final int[]     STAT_SLOTS = {10, 12, 14, 16};
    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;

    public StatAnvilGUI() {
        this.inventory = Bukkit.createInventory(this, 27,
            Component.text("능력치 모루", NamedTextColor.DARK_PURPLE));
        fillBackground();
        render();
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void fillBackground() {
        for (int i = 0; i < 27; i++) inventory.setItem(i, BACKGROUND);
    }

    private void render() {
        Stat[] stats = Stat.values();
        for (int i = 0; i < STAT_SLOTS.length; i++) {
            inventory.setItem(STAT_SLOTS[i], buildStatItem(stats[i]));
        }
    }

    private ItemStack buildStatItem(Stat stat) {
        ItemStack item = new ItemStack(stat.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(stat.getDisplayName(), stat.getColor()));
        meta.lore(List.of(Component.text(stat.getDescription(), NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    public Stat getStatAt(int rawSlot) {
        for (int i = 0; i < STAT_SLOTS.length; i++) {
            if (STAT_SLOTS[i] == rawSlot) return Stat.values()[i];
        }
        return null;
    }

    public void open(Player player) { player.openInventory(inventory); }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
