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

        if (slot == GameModeSelectGUI.BASE_MODE_SLOT) {
            GameManager gm = Asurajang.getInstance().getGameManager();
            boolean enabled = !gm.isBaseModeEnabled();
            gm.setBaseModeEnabled(enabled);
            event.getInventory().setItem(GameModeSelectGUI.BASE_MODE_SLOT, GameModeSelectGUI.buildBaseModeItem(enabled));
            // 기지 모드를 끄면 거점 공격 버튼도 함께 감춘다
            event.getInventory().setItem(GameModeSelectGUI.GUARDIAN_ATTACK_SLOT, enabled
                ? GameModeSelectGUI.buildGuardianAttackItem(gm.isGuardianAttackEnabled())
                : GameModeSelectGUI.backgroundItem());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, enabled ? 1.4f : 0.8f);
            return;
        }

        if (slot == GameModeSelectGUI.GUARDIAN_ATTACK_SLOT) {
            GameManager gm = Asurajang.getInstance().getGameManager();
            if (!gm.isBaseModeEnabled()) return; // 기지 모드가 꺼져 있으면 보이지 않는 버튼이므로 무시

            boolean enabled = !gm.isGuardianAttackEnabled();
            gm.setGuardianAttackEnabled(enabled);
            event.getInventory().setItem(GameModeSelectGUI.GUARDIAN_ATTACK_SLOT, GameModeSelectGUI.buildGuardianAttackItem(enabled));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, enabled ? 1.4f : 0.8f);
            return;
        }

        if (slot != GameModeSelectGUI.TEAM_SLOT && slot != GameModeSelectGUI.SOLO_SLOT) return;

        player.closeInventory();

        GameManager.GameMode mode = (slot == GameModeSelectGUI.TEAM_SLOT)
            ? GameManager.GameMode.TEAM
            : GameManager.GameMode.SOLO;

        if (!Asurajang.getInstance().getGameManager().start(mode, player)) {
            player.sendMessage(Component.text("이미 게임이 진행 중이거나 준비 중입니다.", NamedTextColor.RED));
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
    }
}
