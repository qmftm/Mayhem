package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.gui.GameModeSelectGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GameModeSelectListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GameModeSelectGUI)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot != GameModeSelectGUI.TEAM_SLOT && slot != GameModeSelectGUI.SOLO_SLOT) return;

        player.closeInventory();

        GameManager.GameMode mode = (slot == GameModeSelectGUI.TEAM_SLOT)
            ? GameManager.GameMode.TEAM
            : GameManager.GameMode.SOLO;

        if (!Asurajang.getInstance().getGameManager().start(mode)) {
            player.sendMessage(Component.text("이미 게임이 진행 중이거나 준비 중입니다.", NamedTextColor.RED));
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
    }
}
