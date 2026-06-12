package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
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
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TotalConcentrationEffect implements AugmentationEffect {

    private static final List<String> ENCHANTMENTS = List.of(
        "sharpness", "knockback", "fire_aspect", "sweeping_edge"
    );

    private static final double ENCHANT_CHANCE = 0.25;

    @Override
    public void onActivate(Player player) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = sword.getItemMeta();

        meta.displayName(Component.text("전 집중 호흡", NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));

        meta.getPersistentDataContainer().set(
            Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING, "TotalConcentration");

        sword.setItemMeta(meta);
        rerollEnchant(sword);
        player.getInventory().addItem(sword);
    }

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onKillEnemy(Player player, Player victim) {
        rerollSwordIn(player);
    }

    @Override
    public void onOwnerDeath(Player player) {
        rerollSwordIn(player);
    }

    private void rerollSwordIn(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            if (!"TotalConcentration".equals(
                    meta.getPersistentDataContainer().get(Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING))) {
                continue;
            }
            rerollEnchant(item);
        }
    }

    private static void rerollEnchant(ItemStack sword) {
        ItemMeta meta = sword.getItemMeta();

        for (Enchantment ench : new HashSet<>(meta.getEnchants().keySet())) {
            meta.removeEnchant(ench);
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> keys = new ArrayList<>(ENCHANTMENTS);
        Collections.shuffle(keys, random);

        boolean applied = false;
        for (int i = 0; i < keys.size(); i++) {
            boolean forced = !applied && i == keys.size() - 1;
            if (!forced && random.nextDouble() >= ENCHANT_CHANCE) continue;

            TypedKey<Enchantment> typedKey = TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.minecraft(keys.get(i)));
            Enchantment ench = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(typedKey);
            if (ench == null) continue;

            int level = random.nextInt(ench.getMaxLevel()) + 1;
            meta.addEnchant(ench, level, true);
            applied = true;
        }

        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
    }
}
