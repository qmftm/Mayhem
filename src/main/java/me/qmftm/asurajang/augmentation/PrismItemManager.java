package me.qmftm.asurajang.augmentation;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrismItemManager {

    private final List<PrismChoice.Item> items = new ArrayList<>();

    public PrismItemManager(FileConfiguration config) {
        load(config);
    }

    public void reload(FileConfiguration config) {
        load(config);
    }

    private void load(FileConfiguration config) {
        items.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection entry = config.getConfigurationSection(id);
            if (entry == null) continue;

            String matStr = entry.getString("material", "STONE");
            Material material = Material.matchMaterial(matStr);
            if (material == null) continue;

            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();

            String name = entry.getString("name");
            if (name != null) {
                meta.displayName(Component.text(name, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            }

            ConfigurationSection enchSection = entry.getConfigurationSection("enchantments");
            if (enchSection != null) {
                for (String enchKey : enchSection.getKeys(false)) {
                    int level = enchSection.getInt(enchKey, 1);
                    TypedKey<Enchantment> typedKey = TypedKey.create(
                        RegistryKey.ENCHANTMENT, NamespacedKey.minecraft(enchKey.toLowerCase()));
                    Enchantment ench = RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT).get(typedKey);
                    if (ench != null) meta.addEnchant(ench, level, true);
                }
            }

            stack.setItemMeta(meta);
            items.add(new PrismChoice.Item(stack));
        }
    }

    public List<PrismChoice.Item> getAll() {
        return Collections.unmodifiableList(items);
    }
}
