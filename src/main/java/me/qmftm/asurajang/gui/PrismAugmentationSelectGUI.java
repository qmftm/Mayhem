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
import org.jetbrains.annotations.NotNull;

import java.util.List;

// 프리즘 증강 선택 메뉴. 프리즘 증강 자체는 아직 구현되지 않아 안내만 표시한다.
public class PrismAugmentationSelectGUI implements InventoryHolder {

    private static final int INFO_SLOT = 13;

    private final Inventory inventory;

    private static final ItemStack BACKGROUND = buildBackground();
    private static final ItemStack INFO_ITEM = buildInfoItem();

    public PrismAugmentationSelectGUI() {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("프리즘 증강 선택", NamedTextColor.LIGHT_PURPLE));
        fillBackground();
        inventory.setItem(INFO_SLOT, INFO_ITEM);
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack buildInfoItem() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("프리즘 증강", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("아직 준비 중인 기능입니다.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("추후 업데이트를 기대해주세요!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, BACKGROUND);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
