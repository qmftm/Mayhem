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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AugmentationSelectGUI implements InventoryHolder {

    private static final int[] AUG_SLOTS    = {11, 13, 15};
    private static final int[] REROLL_SLOTS = {20, 22, 24};
    private static final int MAX_REROLLS = 3;

    private final Inventory inventory;
    private final List<Augmentation> pool;
    private final Augmentation[] current = new Augmentation[3];
    private final int[] rerollsLeft = {MAX_REROLLS, MAX_REROLLS, MAX_REROLLS};

    private static final ItemStack BACKGROUND = buildBackground();

    public AugmentationSelectGUI(List<Augmentation> pool) {
        this.pool = new ArrayList<>(pool);
        this.inventory = Bukkit.createInventory(this, 36, Component.text("증강 선택"));
        fillBackground();
        randomizeAll();
        render();
    }

    // ── 초기화 ──────────────────────────────────────────────────────────────

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
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

    private void randomizeAll() {
        List<Augmentation> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        for (int i = 0; i < current.length && i < shuffled.size(); i++) {
            current[i] = shuffled.get(i);
        }
    }

    // ── 렌더 ────────────────────────────────────────────────────────────────

    private void render() {
        for (int i = 0; i < AUG_SLOTS.length; i++) {
            inventory.setItem(AUG_SLOTS[i], current[i] != null ? current[i].getIcon().clone() : BACKGROUND);
            inventory.setItem(REROLL_SLOTS[i], buildRerollButton(i));
        }
    }

    private ItemStack buildRerollButton(int index) {
        int left = rerollsLeft[index];
        Material mat;
        NamedTextColor color;
        String label;

        switch (left) {
            case 3 -> { mat = Material.LIME_DYE;   color = NamedTextColor.GREEN;  label = "리롤  3 / 3"; }
            case 2 -> { mat = Material.YELLOW_DYE; color = NamedTextColor.YELLOW; label = "리롤  2 / 3"; }
            case 1 -> { mat = Material.ORANGE_DYE; color = NamedTextColor.GOLD;   label = "리롤  1 / 3  (마지막)"; }
            default -> { mat = Material.BARRIER;   color = NamedTextColor.RED;    label = "리롤 불가"; }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, color));
        item.setItemMeta(meta);
        return item;
    }

    // ── 리롤 ────────────────────────────────────────────────────────────────

    public boolean reroll(int slotIndex) {
        if (rerollsLeft[slotIndex] <= 0) return false;
        rerollsLeft[slotIndex]--;

        List<Augmentation> available = new ArrayList<>(pool);
        for (Augmentation aug : current) {
            available.remove(aug);
        }
        if (available.isEmpty()) available = new ArrayList<>(pool);
        current[slotIndex] = available.get(ThreadLocalRandom.current().nextInt(available.size()));

        render();
        return true;
    }

    // ── 슬롯 조회 ───────────────────────────────────────────────────────────

    @Nullable
    public Augmentation getAugAt(int rawSlot) {
        for (int i = 0; i < AUG_SLOTS.length; i++) {
            if (AUG_SLOTS[i] == rawSlot) return current[i];
        }
        return null;
    }

    public int getRerollIndexAt(int rawSlot) {
        for (int i = 0; i < REROLL_SLOTS.length; i++) {
            if (REROLL_SLOTS[i] == rawSlot) return i;
        }
        return -1;
    }

    // ── 오픈 ────────────────────────────────────────────────────────────────

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
