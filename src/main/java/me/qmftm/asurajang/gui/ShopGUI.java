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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {

    // 검류: 2~6번 슬롯 (1행 중앙)
    private static final int[] WEAPON_SLOTS = {2, 3, 4, 5, 6};
    // 갑옷류: 11~15번 슬롯 (2행 중앙)
    private static final int[] ARMOR_SLOTS  = {11, 12, 13, 14, 15};
    // 활류 + 음식류: 3행 중앙
    private static final int[] BOW_SLOTS    = {20, 21, 22};

    private static final ItemStack[] WEAPON_ITEMS = {
        new ItemStack(Material.STONE_SWORD),
        new ItemStack(Material.IRON_SWORD),
        new ItemStack(Material.DIAMOND_SWORD),
        new ItemStack(Material.NETHERITE_SWORD),
        new ItemStack(Material.DIAMOND_AXE)
    };

    // 킬당 골드(기본 50 + 보너스 최대 약 50)와 시작 골드 200을 기준으로 책정
    private static final int[] WEAPON_PRICES = {50, 100, 250, 500, 250};

    private static final ItemStack[] ARMOR_ITEMS = {
        new ItemStack(Material.IRON_HELMET),
        new ItemStack(Material.IRON_CHESTPLATE),
        new ItemStack(Material.IRON_LEGGINGS),
        new ItemStack(Material.IRON_BOOTS),
        new ItemStack(Material.DIAMOND_CHESTPLATE)
    };

    private static final int[] ARMOR_PRICES = {80, 150, 130, 80, 400};

    private static final ItemStack BOW    = new ItemStack(Material.BOW);
    private static final ItemStack ARROWS = new ItemStack(Material.ARROW, 8);
    private static final ItemStack BREAD  = new ItemStack(Material.BREAD, 8);

    private static final int BOW_PRICE    = 100;
    private static final int ARROWS_PRICE = 40;
    private static final int BREAD_PRICE  = 30;

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;
    private final Map<Integer, ItemStack> purchaseMap = new HashMap<>();
    private final Map<Integer, Integer>   priceMap    = new HashMap<>();

    public ShopGUI() {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("상점"));
        fillBackground();
        populate();
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void fillBackground() {
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, BACKGROUND);
        }
    }

    private void populate() {
        for (int i = 0; i < WEAPON_SLOTS.length; i++) {
            place(WEAPON_SLOTS[i], WEAPON_ITEMS[i], WEAPON_PRICES[i]);
        }
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            place(ARMOR_SLOTS[i], ARMOR_ITEMS[i], ARMOR_PRICES[i]);
        }
        place(BOW_SLOTS[0], BOW, BOW_PRICE);
        place(BOW_SLOTS[1], ARROWS, ARROWS_PRICE);
        place(BOW_SLOTS[2], BREAD, BREAD_PRICE);
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
