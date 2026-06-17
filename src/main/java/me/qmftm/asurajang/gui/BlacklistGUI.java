package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.Augmentation;
import me.qmftm.asurajang.augmentation.BlacklistManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlacklistGUI implements InventoryHolder {

    public static final int SLOT_PREV = 45;
    public static final int SLOT_NEXT = 53;
    private static final int SLOT_PAGE = 49;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int SIZE = 54;

    private final List<Augmentation> augPage1;
    private final List<Augmentation> augPage2;
    private final List<Augmentation> prismList;
    private final List<Augmentation> synergyList;
    private int page;
    private final Inventory inventory;

    public BlacklistGUI() {
        Asurajang plugin = Asurajang.getInstance();
        List<Augmentation> allAug = plugin.getAugmentationManager().getAll();
        List<Augmentation> allPrism = plugin.getAugmentationManager().getPrismAll();
        List<Augmentation> allSynergy = plugin.getSynergyManager().getSynergyAugmentations();

        if (allAug.size() > ITEMS_PER_PAGE) {
            augPage1 = allAug.subList(0, ITEMS_PER_PAGE);
            augPage2 = allAug.subList(ITEMS_PER_PAGE, allAug.size());
        } else {
            augPage1 = allAug;
            augPage2 = List.of();
        }
        prismList = allPrism;
        synergyList = allSynergy;

        this.page = 0;
        this.inventory = Bukkit.createInventory(this, SIZE,
            Component.text("증강 블랙리스트", NamedTextColor.DARK_RED));
        render();
    }

    public int getPage() { return page; }

    public void nextPage() {
        if (page < 3) { page++; render(); }
    }

    public void prevPage() {
        if (page > 0) { page--; render(); }
    }

    public Augmentation getAugAt(int slot) {
        if (slot < 0 || slot >= ITEMS_PER_PAGE) return null;
        List<Augmentation> current = getCurrentList();
        if (slot >= current.size()) return null;
        return current.get(slot);
    }

    private List<Augmentation> getCurrentList() {
        return switch (page) {
            case 0 -> augPage1;
            case 1 -> augPage2;
            case 2 -> prismList;
            case 3 -> synergyList;
            default -> List.of();
        };
    }

    private String getPageLabel() {
        return switch (page) {
            case 0 -> "증강 (1/2)";
            case 1 -> "증강 (2/2)";
            case 2 -> "프리즘";
            case 3 -> "시너지";
            default -> "";
        };
    }

    public void render() {
        BlacklistManager bm = Asurajang.getInstance().getBlacklistManager();

        for (int i = 0; i < SIZE; i++) inventory.setItem(i, buildBackground());

        if (page > 0) inventory.setItem(SLOT_PREV, buildArrow(false));
        inventory.setItem(SLOT_PAGE, buildPageItem());
        if (page < 3) inventory.setItem(SLOT_NEXT, buildArrow(true));

        List<Augmentation> current = getCurrentList();
        for (int i = 0; i < current.size() && i < ITEMS_PER_PAGE; i++) {
            Augmentation aug = current.get(i);
            boolean blocked = bm.isBlacklisted(aug.getId());
            inventory.setItem(i, buildItem(aug, blocked));
        }
    }

    private ItemStack buildItem(Augmentation aug, boolean blocked) {
        ItemStack item = aug.getIcon().clone();
        ItemMeta meta = item.getItemMeta();

        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        if (blocked) {
            lore.add(Component.text("✘ 블랙리스트됨", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("클릭하여 해제", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(Component.text("✔ 활성", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("클릭하여 블랙리스트", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);

        if (blocked) {
            item.setType(Material.GRAY_DYE);
        }

        return item;
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
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

    private ItemStack buildPageItem() {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.displayName(Component.text(getPageLabel() + "  " + (page + 1) + " / 4", NamedTextColor.WHITE)
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
