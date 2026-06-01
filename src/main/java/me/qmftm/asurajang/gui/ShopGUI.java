package me.qmftm.asurajang.gui;

import net.kyori.adventure.text.Component;
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

    private static final ItemStack[] ARMOR_ITEMS = {
        new ItemStack(Material.IRON_HELMET),
        new ItemStack(Material.IRON_CHESTPLATE),
        new ItemStack(Material.IRON_LEGGINGS),
        new ItemStack(Material.IRON_BOOTS),
        new ItemStack(Material.DIAMOND_CHESTPLATE)
    };

    private static final ItemStack BOW    = new ItemStack(Material.BOW);
    private static final ItemStack ARROWS = new ItemStack(Material.ARROW, 8);
    private static final ItemStack BREAD  = new ItemStack(Material.BREAD, 8);

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;
    private final Map<Integer, ItemStack> purchaseMap = new HashMap<>();

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
            place(WEAPON_SLOTS[i], WEAPON_ITEMS[i]);
        }
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            place(ARMOR_SLOTS[i], ARMOR_ITEMS[i]);
        }
        place(BOW_SLOTS[0], BOW);
        place(BOW_SLOTS[1], ARROWS);
        place(BOW_SLOTS[2], BREAD);
    }

    private void place(int slot, ItemStack item) {
        inventory.setItem(slot, item.clone());
        purchaseMap.put(slot, item.clone());
    }

    @Nullable
    public ItemStack getPurchase(int slot) {
        ItemStack item = purchaseMap.get(slot);
        return item != null ? item.clone() : null;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
