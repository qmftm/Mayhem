package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.augmentation.Augmentation;
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

public class AugmentationListGUI implements InventoryHolder {

    public static final int SLOT_PREV = 45;
    public static final int SLOT_NEXT = 53;
    private static final int SLOT_PAGE = 49;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int SIZE = 54;

    private static final ItemStack BACKGROUND = buildBackground();

    private final List<Augmentation> augmentations;
    private int page;
    private final Inventory inventory;

    public AugmentationListGUI(List<Augmentation> augmentations) {
        this(augmentations, Component.text("증강 목록"));
    }

    public AugmentationListGUI(List<Augmentation> augmentations, Component title) {
        this.augmentations = augmentations;
        this.page = 0;
        this.inventory = Bukkit.createInventory(this, SIZE, title);
        render();
    }

    public void nextPage() {
        if (page < totalPages() - 1) { page++; render(); }
    }

    public void prevPage() {
        if (page > 0) { page--; render(); }
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil(augmentations.size() / (double) ITEMS_PER_PAGE));
    }

    private void render() {
        inventory.clear();
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, BACKGROUND);

        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && start + i < augmentations.size(); i++) {
            inventory.setItem(i, augmentations.get(start + i).getIcon().clone());
        }

        int total = totalPages();
        if (page > 0) inventory.setItem(SLOT_PREV, buildArrow(false));
        inventory.setItem(SLOT_PAGE, buildPageItem(page + 1, total));
        if (page < total - 1) inventory.setItem(SLOT_NEXT, buildArrow(true));
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack buildArrow(boolean next) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        meta.displayName(Component.text(next ? "다음 페이지 ▶" : "◀ 이전 페이지", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        arrow.setItemMeta(meta);
        return arrow;
    }

    private static ItemStack buildPageItem(int current, int total) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.displayName(Component.text(current + " / " + total + " 페이지", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        paper.setItemMeta(meta);
        return paper;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
