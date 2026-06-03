package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.augmentation.Augmentation;
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

public class DebugAugGiveGUI implements InventoryHolder {

    private final Inventory inventory;
    private final List<Augmentation> augmentations;

    public DebugAugGiveGUI(List<Augmentation> augmentations) {
        this.augmentations = augmentations;
        int rows = Math.max(1, Math.min(6, (int) Math.ceil(augmentations.size() / 9.0)));
        this.inventory = Bukkit.createInventory(this, rows * 9,
            Component.text("[DEBUG] 증강 지급", NamedTextColor.RED));
        fillBackground();
        populate();
    }

    private void fillBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, pane);
        }
    }

    private void populate() {
        for (int i = 0; i < augmentations.size() && i < inventory.getSize(); i++) {
            inventory.setItem(i, augmentations.get(i).getIcon().clone());
        }
    }

    @Nullable
    public Augmentation getAugAt(int slot) {
        if (slot < 0 || slot >= augmentations.size()) return null;
        return augmentations.get(slot);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
