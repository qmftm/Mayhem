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

public class GameModeSelectGUI implements InventoryHolder {

    public static final int TEAM_SLOT = 11;
    public static final int SOLO_SLOT = 15;

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;

    public GameModeSelectGUI() {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("게임 모드 선택"));
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
        inventory.setItem(TEAM_SLOT, buildChoice(
            Material.BLUE_BANNER,
            Component.text("팀전", NamedTextColor.GREEN),
            Component.text("팀을 나눠 싸우는 모드", NamedTextColor.GRAY)
        ));
        inventory.setItem(SOLO_SLOT, buildChoice(
            Material.BLADE_POTTERY_SHERD,
            Component.text("개인전", NamedTextColor.GOLD),
            Component.text("혼자 싸우는 모드", NamedTextColor.GRAY)
        ));
    }

    private static ItemStack buildChoice(Material material, Component name, Component loreLine) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(loreLine.decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
