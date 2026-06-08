package me.qmftm.asurajang.game;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DefaultKit {

    private record Enchant(TypedKey<Enchantment> key, int level) {}

    private static final ItemStack DIAMOND_HELMET = buildArmor(Material.DIAMOND_HELMET,
        new Enchant(EnchantmentKeys.PROTECTION, 2),
        new Enchant(EnchantmentKeys.UNBREAKING, 2),
        new Enchant(EnchantmentKeys.RESPIRATION, 1)
    );

    private static final ItemStack DIAMOND_CHESTPLATE = buildArmor(Material.DIAMOND_CHESTPLATE,
        new Enchant(EnchantmentKeys.PROTECTION, 2),
        new Enchant(EnchantmentKeys.UNBREAKING, 2)
    );

    private static final ItemStack DIAMOND_LEGGINGS = buildArmor(Material.DIAMOND_LEGGINGS,
        new Enchant(EnchantmentKeys.PROTECTION, 2),
        new Enchant(EnchantmentKeys.UNBREAKING, 2)
    );

    private static final ItemStack DIAMOND_BOOTS = buildArmor(Material.DIAMOND_BOOTS,
        new Enchant(EnchantmentKeys.PROTECTION, 2),
        new Enchant(EnchantmentKeys.UNBREAKING, 2),
        new Enchant(EnchantmentKeys.FEATHER_FALLING, 2)
    );

    private static final ItemStack SHOP_BUTTON = buildItem(
        Material.EMERALD, "상점", NamedTextColor.GREEN,
        List.of("우클릭으로 상점을 엽니다.")
    );

    private static final ItemStack AUG_BUTTON = buildItem(
        Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "증강 선택", NamedTextColor.LIGHT_PURPLE,
        List.of("우클릭으로 증강을 선택합니다.")
    );

    private static final ItemStack PRISM_AUG_BUTTON = buildItem(
        Material.PRISMARINE_CRYSTALS, "프리즘 증강 선택", NamedTextColor.LIGHT_PURPLE,
        List.of("우클릭으로 프리즘 증강을 선택합니다.")
    );

    public static void apply(Player player) {
        // 체력·배고픔 완전 회복
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) player.setHealth(maxHealth.getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);

        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setHelmet(DIAMOND_HELMET.clone());
        inv.setChestplate(DIAMOND_CHESTPLATE.clone());
        inv.setLeggings(DIAMOND_LEGGINGS.clone());
        inv.setBoots(DIAMOND_BOOTS.clone());

        inv.setItem(0, new ItemStack(Material.WOODEN_SWORD));
        inv.setItem(6, PRISM_AUG_BUTTON.clone());
        inv.setItem(7, SHOP_BUTTON.clone());
        inv.setItem(8, AUG_BUTTON.clone());
    }

    private static ItemStack buildItem(Material material, String name, NamedTextColor color, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(loreLines.stream()
            .map(l -> Component.text(l, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
            .toList());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildArmor(Material material, Enchant... enchants) {
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
}
