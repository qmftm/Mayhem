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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ExecutionerSwordEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {
        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = sword.getItemMeta();

        meta.displayName(Component.text("처형인의 검", NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));

        TypedKey<Enchantment> sharpnessKey = TypedKey.create(
            RegistryKey.ENCHANTMENT, NamespacedKey.minecraft("sharpness"));
        Enchantment sharpness = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT).get(sharpnessKey);
        if (sharpness != null) meta.addEnchant(sharpness, 67, true);

        if (meta instanceof Damageable damageable) {
            damageable.setDamage(Material.GOLDEN_SWORD.getMaxDurability() - 1);
        }

        sword.setItemMeta(meta);
        player.getInventory().addItem(sword);
    }

    @Override
    public void onDeactivate(Player player) {}
}
