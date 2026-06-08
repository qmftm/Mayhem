package me.qmftm.asurajang.gui;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 프리즘 증강 선택 메뉴. 클릭한 부위의 인챈트 다이아 갑옷을 그대로 지급한다.
public class PrismAugmentationSelectGUI implements InventoryHolder {

    private static final int[] ITEM_SLOTS = {11, 12, 13, 14, 15};

    private final Inventory inventory;

    private static final ItemStack BACKGROUND = buildBackground();
    private static final ItemStack[] ITEMS = {
        buildEnchanted(Material.DIAMOND_HELMET,
            new Enchant(EnchantmentKeys.PROTECTION, 2),
            new Enchant(EnchantmentKeys.UNBREAKING, 2),
            new Enchant(EnchantmentKeys.RESPIRATION, 1)
        ),
        buildEnchanted(Material.DIAMOND_CHESTPLATE,
            new Enchant(EnchantmentKeys.PROTECTION, 2),
            new Enchant(EnchantmentKeys.UNBREAKING, 2)
        ),
        buildEnchanted(Material.DIAMOND_SWORD,
            new Enchant(EnchantmentKeys.SHARPNESS, 2),
            new Enchant(EnchantmentKeys.UNBREAKING, 2),
            new Enchant(EnchantmentKeys.FIRE_ASPECT, 1)
        ),
        buildEnchanted(Material.DIAMOND_LEGGINGS,
            new Enchant(EnchantmentKeys.PROTECTION, 2),
            new Enchant(EnchantmentKeys.UNBREAKING, 2)
        ),
        buildEnchanted(Material.DIAMOND_BOOTS,
            new Enchant(EnchantmentKeys.PROTECTION, 2),
            new Enchant(EnchantmentKeys.UNBREAKING, 2),
            new Enchant(EnchantmentKeys.FEATHER_FALLING, 2)
        ),
    };

    public PrismAugmentationSelectGUI() {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("프리즘 증강 선택", NamedTextColor.LIGHT_PURPLE));
        fillBackground();
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            inventory.setItem(ITEM_SLOTS[i], ITEMS[i].clone());
        }
    }

    @Nullable
    public ItemStack getItemAt(int rawSlot) {
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == rawSlot) return ITEMS[i];
        }
        return null;
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
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

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    // ── 지급 아이템 빌드 ─────────────────────────────────────────────────────

    private record Enchant(TypedKey<Enchantment> key, int level) {}

    private static ItemStack buildEnchanted(Material material, Enchant... enchants) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        for (Enchant enchant : enchants) {
            meta.addEnchant(enchantment(enchant.key()), enchant.level(), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static Enchantment enchantment(TypedKey<Enchantment> key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key);
    }

    // ── 프리즘 비용 ──────────────────────────────────────────────────────────

    private static final ItemStack PRISM = buildPrism();

    public static ItemStack createPrism() {
        return PRISM.clone();
    }

    private static ItemStack buildPrism() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("프리즘", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }
}
