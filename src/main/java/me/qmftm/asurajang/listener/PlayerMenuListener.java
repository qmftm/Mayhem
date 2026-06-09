package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.gui.PlayerMenuGUI;
import me.qmftm.asurajang.gui.ShopGUI;
import me.qmftm.asurajang.gui.StatAnvilGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerMenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayerMenuGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        PlayerMenuGUI.Action action = gui.getActionAt(event.getRawSlot());
        if (action == null) return;

        player.closeInventory();

        switch (action) {
            case SHOP -> new ShopGUI().open(player);

            case AUG -> {
                int charges = Asurajang.getInstance().getLevelUpManager().getAugCharges(player.getUniqueId());
                if (charges <= 0) {
                    player.sendMessage(Component.text("증강 선택 기회가 없습니다.", NamedTextColor.RED));
                    return;
                }
                Asurajang.getInstance().openAugmentationSelect(player);
            }

            case PRISM -> {
                int charges = Asurajang.getInstance().getLevelUpManager().getPrismCharges(player.getUniqueId());
                if (charges <= 0) {
                    player.sendMessage(Component.text("프리즘 증강 선택 기회가 없습니다.", NamedTextColor.RED));
                    return;
                }
                Asurajang.getInstance().openPrismAugmentationSelect(player);
            }

            case ANVIL -> {
                boolean hasAnvil = hasStatAnvil(player);
                if (!hasAnvil) {
                    player.sendMessage(Component.text("능력치 모루가 없습니다.", NamedTextColor.RED));
                    return;
                }
                new StatAnvilGUI().open(player);
            }
        }
    }

    private boolean hasStatAnvil(Player player) {
        for (var it : player.getInventory().getContents()) {
            if (it == null || !it.hasItemMeta()) continue;
            if (it.getItemMeta().getPersistentDataContainer()
                    .has(Asurajang.STAT_ANVIL_KEY, org.bukkit.persistence.PersistentDataType.BYTE)) {
                return true;
            }
        }
        return false;
    }
}
