package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerMenuGUI implements InventoryHolder {

    private static final int SLOT_SHOP    = 10;
    private static final int SLOT_AUG     = 12;
    private static final int SLOT_PRISM   = 14;
    private static final int SLOT_ANVIL   = 16;
    private static final int SLOT_MY_AUGS = 22;

    public enum Action { SHOP, AUG, PRISM, ANVIL, MY_AUGS }

    private final Inventory inventory;

    public PlayerMenuGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, buildTitle(player));
        fill(player);
    }

    private Component buildTitle(Player player) {
        double maxHp = attr(player, Attribute.MAX_HEALTH);
        double atk   = attr(player, Attribute.ATTACK_DAMAGE);
        double spd   = attr(player, Attribute.MOVEMENT_SPEED);
        return Component.text()
            .append(Component.text("체력 " + fmt1(maxHp), NamedTextColor.DARK_RED))
            .append(Component.text("  공격력 " + fmt2(atk), NamedTextColor.GOLD))
            .append(Component.text("  속도 " + fmt2(spd), NamedTextColor.DARK_AQUA))
            .build();
    }

    private void fill(Player player) {
        ItemStack bg = buildBg();
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        var lum = Asurajang.getInstance().getLevelUpManager();
        int augCharges   = lum.getAugCharges(player.getUniqueId());
        int prismCharges = lum.getPrismCharges(player.getUniqueId());
        int anvilCount   = lum.getAnvilCharges(player.getUniqueId());

        inventory.setItem(SLOT_SHOP,  buildEntry(Material.EMERALD,
            "상점", NamedTextColor.DARK_GREEN,
            List.of(Component.text("클릭으로 상점을 엽니다.", NamedTextColor.GRAY)), true));

        inventory.setItem(SLOT_AUG, buildEntry(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
            "증강 선택", NamedTextColor.DARK_PURPLE,
            List.of(Component.text("남은 기회: ", NamedTextColor.GRAY)
                .append(Component.text(augCharges + "회", augCharges > 0 ? NamedTextColor.DARK_PURPLE : NamedTextColor.DARK_GRAY))),
            augCharges > 0));

        inventory.setItem(SLOT_PRISM, buildEntry(Material.PRISMARINE_CRYSTALS,
            "프리즘 증강 선택", NamedTextColor.DARK_AQUA,
            List.of(Component.text("남은 기회: ", NamedTextColor.GRAY)
                .append(Component.text(prismCharges + "회", prismCharges > 0 ? NamedTextColor.DARK_AQUA : NamedTextColor.DARK_GRAY))),
            prismCharges > 0));

        inventory.setItem(SLOT_ANVIL, buildEntry(Material.ANVIL,
            "능력치 모루", NamedTextColor.DARK_PURPLE,
            List.of(Component.text("보유 수량: ", NamedTextColor.GRAY)
                .append(Component.text(anvilCount + "개", anvilCount > 0 ? NamedTextColor.DARK_PURPLE : NamedTextColor.DARK_GRAY))),
            anvilCount > 0));

        inventory.setItem(SLOT_MY_AUGS, buildEntry(Material.ENCHANTED_BOOK,
            "내 증강 목록", NamedTextColor.LIGHT_PURPLE,
            List.of(Component.text("클릭으로 보유한 증강을 확인합니다.", NamedTextColor.GRAY)), true));
    }

    private ItemStack buildBg() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack buildEntry(Material mat, String name, NamedTextColor color,
                                  List<Component> lore, boolean active) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, active ? color : NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.stream()
            .map(l -> l.decoration(TextDecoration.ITALIC, false))
            .toList());
        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    public Action getActionAt(int slot) {
        return switch (slot) {
            case SLOT_SHOP    -> Action.SHOP;
            case SLOT_AUG     -> Action.AUG;
            case SLOT_PRISM   -> Action.PRISM;
            case SLOT_ANVIL   -> Action.ANVIL;
            case SLOT_MY_AUGS -> Action.MY_AUGS;
            default           -> null;
        };
    }

    private double attr(Player player, Attribute attribute) {
        AttributeInstance inst = player.getAttribute(attribute);
        return inst != null ? inst.getValue() : 0;
    }

    private String fmt1(double v) { return String.format("%.0f", v); }
    private String fmt2(double v) { return String.format("%.2f", v); }

    public void open(Player player) { player.openInventory(inventory); }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
