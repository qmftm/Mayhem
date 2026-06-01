package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.augmentation.Augmentation;
import net.kyori.adventure.text.Component;
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

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;

    public AugmentationListGUI(List<Augmentation> augmentations) {
        this(augmentations, Component.text("증강 목록"));
    }

    public AugmentationListGUI(List<Augmentation> augmentations, Component title) {
        int rows = Math.max(1, Math.min(6, (int) Math.ceil(augmentations.size() / 9.0)));
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        fillBackground();
        populate(augmentations);
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void fillBackground() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, BACKGROUND);
        }
    }

    private void populate(List<Augmentation> augmentations) {
        for (int i = 0; i < augmentations.size() && i < inventory.getSize(); i++) {
            inventory.setItem(i, augmentations.get(i).getIcon().clone());
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
