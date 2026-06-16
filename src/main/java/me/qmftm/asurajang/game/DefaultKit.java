package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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

        GameManager gm = Asurajang.getInstance().getGameManager();
        if (gm.getBaseMode() == GameManager.BaseMode.WILD && WildKitManager.applyIfExists(player)) {
            inv.setItem(8, buildMenuButton(player));
            return;
        }

        Color armorColor = teamArmorColor(player);
        inv.setHelmet(dyedLeather(Material.LEATHER_HELMET, armorColor));
        inv.setChestplate(dyedLeather(Material.LEATHER_CHESTPLATE, armorColor));
        inv.setLeggings(dyedLeather(Material.LEATHER_LEGGINGS, armorColor));
        inv.setBoots(dyedLeather(Material.LEATHER_BOOTS, armorColor));

        inv.setItem(0, new ItemStack(Material.WOODEN_SWORD));
        inv.setItem(8, buildMenuButton(player));
    }

    // 팀전일 때 소속 팀 색상을 반환 (개인전 등 팀이 없으면 null = 기본 가죽 색상)
    private static Color teamArmorColor(Player player) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        if (gm.getGameMode() != GameManager.GameMode.TEAM) return null;
        return switch (gm.getTeam(player.getUniqueId())) {
            case 0 -> Color.fromRGB(255, 70, 70);
            case 1 -> Color.fromRGB(80, 130, 255);
            default -> null;
        };
    }

    private static ItemStack dyedLeather(Material type, Color color) {
        ItemStack item = new ItemStack(type);
        if (color != null) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
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
