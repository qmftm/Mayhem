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
    public static final int BASE_MODE_SLOT = 22;

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;

    public GameModeSelectGUI(boolean baseModeEnabled) {
        this.inventory = Bukkit.createInventory(this, 36, Component.text("게임 모드 선택"));
        fillBackground();
        populate(baseModeEnabled);
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void fillBackground() {
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, BACKGROUND);
        }
    }

    private void populate(boolean baseModeEnabled) {
        inventory.setItem(TEAM_SLOT, buildChoice(
            Material.MUSIC_DISC_PRECIPICE,
            Component.text("팀전", NamedTextColor.GREEN),
            List.of(
                Component.text("레드팀과 블루팀으로 나뉘어 싸웁니다.", NamedTextColor.GRAY),
                Component.text("같은 팀끼리는 서로 공격할 수 없습니다.", NamedTextColor.GRAY),
                Component.text("기지 모드를 함께 켤 수 있습니다.", NamedTextColor.DARK_GRAY)
            )
        ));
        inventory.setItem(SOLO_SLOT, buildChoice(
            Material.BLADE_POTTERY_SHERD,
            Component.text("개인전", NamedTextColor.GOLD),
            List.of(
                Component.text("모두가 적이 되어 싸웁니다.", NamedTextColor.GRAY),
                Component.text("전장 곳곳에 무작위로 흩어져 시작합니다.", NamedTextColor.GRAY)
            )
        ));
        inventory.setItem(BASE_MODE_SLOT, buildBaseModeItem(baseModeEnabled));
    }

    public static ItemStack buildBaseModeItem(boolean enabled) {
        return buildChoice(
            enabled ? Material.BEACON : Material.OBSIDIAN,
            Component.text("기지 모드", NamedTextColor.AQUA),
            List.of(
                Component.text("팀 진영에 거점이 세워지고", NamedTextColor.GRAY),
                Component.text("거점을 지키는 가디언을 쓰러뜨리면 거점이 파괴됩니다.", NamedTextColor.GRAY),
                Component.text("팀전을 선택했을 때만 적용됩니다.", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("현재: ", NamedTextColor.GRAY)
                    .append(enabled
                        ? Component.text("켜짐", NamedTextColor.GREEN)
                        : Component.text("꺼짐", NamedTextColor.RED))
            )
        );
    }

    private static ItemStack buildChoice(Material material, Component name, List<Component> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(loreLines.stream()
            .map(line -> line.decoration(TextDecoration.ITALIC, false))
            .toList());
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
