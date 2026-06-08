package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.gui.ShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HotbarButtonListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 메인 핸드만 처리
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = plainName(item.getItemMeta());

        if (item.getType() == Material.EMERALD && "상점".equals(name)) {
            event.setCancelled(true);
            new ShopGUI().open(player);

        } else if (item.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE && "증강 선택".equals(name)) {
            event.setCancelled(true);
            Asurajang.getInstance().openAugmentationSelect(player);

        } else if (item.getType() == Material.PRISMARINE_CRYSTALS && "프리즘 증강 선택".equals(name)) {
            event.setCancelled(true);
            Asurajang.getInstance().openPrismAugmentationSelect(player);
        }
    }

    private String plainName(ItemMeta meta) {
        if (!meta.hasDisplayName()) return "";
        Component name = meta.displayName();
        return name != null ? PlainTextComponentSerializer.plainText().serialize(name) : "";
    }
}
