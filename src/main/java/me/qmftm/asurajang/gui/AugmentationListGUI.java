package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.Asurajang;
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
import org.bukkit.scheduler.BukkitTask;
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
    private BukkitTask loadTask;

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
        if (loadTask != null) { loadTask.cancel(); loadTask = null; }

        for (int i = 0; i < SIZE; i++) inventory.setItem(i, BACKGROUND);

        int total = totalPages();
        if (page > 0) inventory.setItem(SLOT_PREV, buildArrow(false));
        inventory.setItem(SLOT_PAGE, buildPageItem(page + 1, total));
        if (page < total - 1) inventory.setItem(SLOT_NEXT, buildArrow(true));

        int start = page * ITEMS_PER_PAGE;
        int count = Math.min(ITEMS_PER_PAGE, augmentations.size() - start);
        if (count == 0) return;

        int[] cursor = {0};
        loadTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> {
            for (int j = 0; j < 2 && cursor[0] < count; j++, cursor[0]++) {
                inventory.setItem(cursor[0], augmentations.get(start + cursor[0]).getIcon().clone());
            }
            if (cursor[0] >= count) {
                loadTask.cancel();
                loadTask = null;
            }
        }, 1L, 1L);
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
