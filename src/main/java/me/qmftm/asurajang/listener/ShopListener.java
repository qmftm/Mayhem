package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.game.GameScoreboardManager;
import me.qmftm.asurajang.gui.ShopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ShopGUI.Category category = gui.getCategoryHeaderAt(event.getRawSlot());
        if (category != null) {
            gui.toggleCategory(category);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            return;
        }

        ItemStack purchase = gui.getPurchase(event.getRawSlot());
        if (purchase == null) return;

        int price = gui.getPrice(event.getRawSlot());
        GameScoreboardManager scoreboard = Asurajang.getInstance().getScoreboardManager();
        int gold = scoreboard.getGold(player);

        if (gold < price) {
            player.sendMessage(Component.text(
                "골드가 부족합니다. (보유 " + gold + " G / 필요 " + price + " G)", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        scoreboard.addGold(player, -price);
        scoreboard.updatePlayer(player, Asurajang.getInstance().getGameManager().getRemainingSeconds());

        player.getInventory().addItem(purchase);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);
    }
}
