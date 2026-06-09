package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class DefaultKit {

    public static void apply(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) player.setHealth(maxHealth.getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);

        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
        inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));

        inv.setItem(0, new ItemStack(Material.WOODEN_SWORD));
        inv.setItem(8, buildMenuButton(player));
    }

    private static ItemStack buildMenuButton(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(Component.text("메뉴", NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("우클릭으로 메뉴를 엽니다.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(Asurajang.PLAYER_MENU_KEY, PersistentDataType.BYTE, (byte) 1);
        skull.setItemMeta(meta);
        return skull;
    }
}
