package me.qmftm.asurajang.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {

    public enum Category {
        WEAPON("무기", Material.DIAMOND_SWORD,
            new ItemStack[]{
                new ItemStack(Material.STONE_SWORD),
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.NETHERITE_SWORD),
                new ItemStack(Material.DIAMOND_AXE)
            },
            new int[]{100, 200, 450, 700, 650}),

        ARMOR("방어구", Material.DIAMOND_CHESTPLATE,
            new ItemStack[]{
                new ItemStack(Material.IRON_HELMET),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.DIAMOND_CHESTPLATE)
            },
            new int[]{150, 200, 175, 150, 400}),

        RANGED("원거리", Material.CROSSBOW,
            new ItemStack[]{
                new ItemStack(Material.BOW),
                new ItemStack(Material.CROSSBOW),
                new ItemStack(Material.ARROW, 8)
            },
            new int[]{100, 250, 40}),

        MISC("기타", Material.BREAD,
            new ItemStack[]{
                new ItemStack(Material.BREAD, 8),
                strengthPotion(),
                regenerationPotion(),
                new ItemStack(Material.TOTEM_OF_UNDYING)
            },
            new int[]{30, 150, 120, 1000});

        private final String label;
        private final Material icon;
        private final ItemStack[] items;
        private final int[] prices;

        Category(String label, Material icon, ItemStack[] items, int[] prices) {
            this.label = label;
            this.icon = icon;
            this.items = items;
            this.prices = prices;
        }
    }

    private static ItemStack strengthPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.STRENGTH);
        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack regenerationPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.REGENERATION);
        potion.setItemMeta(meta);
        return potion;
    }

    private static final ItemStack BACKGROUND = buildBackground();
    private static final int COLUMNS = 9;

    private final Inventory inventory;
    private final EnumSet<Category> expanded = EnumSet.allOf(Category.class);
    private final Map<Integer, ItemStack> purchaseMap = new HashMap<>();
    private final Map<Integer, Integer>   priceMap    = new HashMap<>();

    public ShopGUI() {
        this.inventory = Bukkit.createInventory(this, Category.values().length * COLUMNS, Component.text("상점"));
        render();
    }

    private void render() {
        inventory.clear();
        purchaseMap.clear();
        priceMap.clear();

        for (Category category : Category.values()) {
            int row = category.ordinal() * COLUMNS;
            boolean isOpen = expanded.contains(category);
            inventory.setItem(row, buildHeader(category, isOpen));

            for (int col = 1; col < COLUMNS; col++) {
                int slot = row + col;
                int index = col - 1;
                if (isOpen && index < category.items.length) {
                    place(slot, category.items[index], category.prices[index]);
                } else {
                    inventory.setItem(slot, BACKGROUND);
                }
            }
        }
    }

    private static ItemStack buildHeader(Category category, boolean isOpen) {
        ItemStack item = new ItemStack(category.icon);
        ItemMeta meta = item.getItemMeta();
        String prefix = isOpen ? "▼ " : "▶ ";
        meta.displayName(Component.text(prefix + category.label, NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(isOpen ? "클릭하여 접기" : "클릭하여 펼치기", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void place(int slot, ItemStack item, int price) {
        inventory.setItem(slot, withPriceLore(item, price));
        purchaseMap.put(slot, item.clone());
        priceMap.put(slot, price);
    }

    private static ItemStack withPriceLore(ItemStack item, int price) {
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        meta.lore(List.of(Component.text(price + " G", NamedTextColor.GOLD)));
        display.setItemMeta(meta);
        return display;
    }

    @Nullable
    public Category getCategoryHeaderAt(int slot) {
        for (Category category : Category.values()) {
            if (slot == category.ordinal() * COLUMNS) {
                return category;
            }
        }
        return null;
    }

    public void toggleCategory(Category category) {
        if (!expanded.remove(category)) {
            expanded.add(category);
        }
        render();
    }

    @Nullable
    public ItemStack getPurchase(int slot) {
        ItemStack item = purchaseMap.get(slot);
        return item != null ? item.clone() : null;
    }

    public int getPrice(int slot) {
        return priceMap.getOrDefault(slot, 0);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
