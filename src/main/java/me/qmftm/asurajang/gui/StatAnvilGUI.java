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
        BULKY   ("맷집", Material.HEART_POTTERY_SHERD,    2.0, 0.2, 0.005, NamedTextColor.RED,
                 List.of("§7+2 최대 체력 (1 하트)", "§7+0.2 공격력", "§7+0.005 이동속도")),
        STRENGTH("근력", Material.BLADE_POTTERY_SHERD,    0.5, 0.5, 0.005, NamedTextColor.GOLD,
                 List.of("§7+0.5 최대 체력 (0.25 하트)", "§7+0.5 공격력", "§7+0.005 이동속도")),
        AGILITY ("민첩", Material.EXPLORER_POTTERY_SHERD, 0.5, 0.2, 0.01, NamedTextColor.AQUA,
                 List.of("§7+0.5 최대 체력 (0.25 하트)", "§7+0.2 공격력", "§7+0.01 이동속도"));

        private final String         displayName;
        private final Material       icon;
        private final double         hpBonus;
        private final double         attackBonus;
        private final double         speedBonus;
        private final NamedTextColor color;
        private final List<String>   loreLines;

        Stat(String displayName, Material icon, double hpBonus, double attackBonus, double speedBonus,
             NamedTextColor color, List<String> loreLines) {
            this.displayName = displayName;
            this.icon        = icon;
            this.hpBonus     = hpBonus;
            this.attackBonus = attackBonus;
            this.speedBonus  = speedBonus;
            this.color       = color;
            this.loreLines   = loreLines;
        }

        public String         getDisplayName() { return displayName; }
        public Material       getIcon()        { return icon; }
        public double         getHpBonus()     { return hpBonus; }
        public double         getAttackBonus() { return attackBonus; }
        public double         getSpeedBonus()  { return speedBonus; }
        public NamedTextColor getColor()       { return color; }
        public List<String>   getLoreLines()   { return loreLines; }
    }

    private static final int[]     STAT_SLOTS = {11, 13, 15};
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
        meta.lore(stat.getLoreLines().stream()
            .map(Component::text)
            .toList());
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
