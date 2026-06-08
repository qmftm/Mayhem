package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.Augmentation;
import me.qmftm.asurajang.gui.AugmentationListGUI;
import me.qmftm.asurajang.gui.AugmentationSelectGUI;
import me.qmftm.asurajang.gui.DebugAugGiveGUI;
import me.qmftm.asurajang.gui.PrismAugmentationSelectGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AugmentationSelectListener implements Listener {

    @EventHandler
    public void onListClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AugmentationListGUI)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPrismSelectClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PrismAugmentationSelectGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = gui.getItemAt(event.getRawSlot());
        if (item == null) return;

        ItemStack prism = PrismAugmentationSelectGUI.createPrism();
        if (!player.getInventory().containsAtLeast(prism, 1)) {
            player.sendMessage(Component.text("[프리즘] 이(가) 부족합니다.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        player.getInventory().removeItem(prism);
        player.closeInventory();
        player.getInventory().addItem(item.clone());
        player.sendMessage(Component.text("[프리즘] 1개를 사용해 아이템을 획득했습니다.", NamedTextColor.LIGHT_PURPLE));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    }

    @EventHandler
    public void onDebugAugGiveClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DebugAugGiveGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Augmentation aug = gui.getAugAt(event.getRawSlot());
        if (aug == null) return;

        boolean alreadyOwned = Asurajang.getInstance()
            .getAugmentationManager()
            .getActiveEffects(player.getUniqueId())
            .containsKey(aug.getId());

        if (alreadyOwned) {
            player.sendMessage(Component.text("[" + aug.getDisplayName() + "] 이미 보유한 증강입니다.", NamedTextColor.GRAY));
            return;
        }

        Asurajang.getInstance().getAugmentationManager().activateFor(player, aug.getId());
        player.sendMessage(Component.text("[DEBUG] [" + aug.getDisplayName() + "] 증강을 획득했습니다.", NamedTextColor.LIGHT_PURPLE));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AugmentationSelectGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // 증강 선택
        Augmentation aug = gui.getAugAt(slot);
        if (aug != null) {
            player.closeInventory();
            Asurajang.getInstance().getAugmentationManager().activateFor(player, aug.getId());
            player.sendMessage(Component.text("[" + aug.getDisplayName() + "] 증강을 획득했습니다.", NamedTextColor.LIGHT_PURPLE));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
            return;
        }

        // 리롤
        int rerollIndex = gui.getRerollIndexAt(slot);
        if (rerollIndex == -1) return;

        if (!gui.reroll(rerollIndex)) {
            player.sendMessage(Component.text("리롤 횟수를 모두 사용했습니다.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.3f);
        }
    }
}
