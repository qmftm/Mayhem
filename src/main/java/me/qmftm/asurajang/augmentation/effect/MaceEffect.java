package me.qmftm.asurajang.augmentation.effect;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MaceEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();

        meta.displayName(Component.text("철퇴", NamedTextColor.DARK_RED)
            .decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);

        addEnchant(meta, "density", 5);
        addEnchant(meta, "breach", 4);

        mace.setItemMeta(meta);
        player.getInventory().addItem(mace);
    }

    @Override
    public void onDeactivate(Player player) {}

    private void addEnchant(ItemMeta meta, String name, int level) {
        TypedKey<Enchantment> key = TypedKey.create(
            RegistryKey.ENCHANTMENT, NamespacedKey.minecraft(name));
        Enchantment enchant = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT).get(key);
        if (enchant != null) meta.addEnchant(enchant, level, true);
    }
}
