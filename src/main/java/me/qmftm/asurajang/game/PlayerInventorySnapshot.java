package me.qmftm.asurajang.game;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventorySnapshot {

    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    private final ItemStack offhand;
    private final GameMode gameMode;

    public PlayerInventorySnapshot(Player player) {
        this.contents      = deepClone(player.getInventory().getContents());
        this.armorContents = deepClone(player.getInventory().getArmorContents());
        ItemStack oh       = player.getInventory().getItemInOffHand();
        this.offhand       = oh.getType() != Material.AIR ? oh.clone() : null;
        this.gameMode      = player.getGameMode();
    }

    public void restore(Player player) {
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents);
        player.getInventory().setItemInOffHand(offhand != null ? offhand.clone() : new ItemStack(Material.AIR));
        player.setGameMode(gameMode);
    }

    private static ItemStack[] deepClone(ItemStack[] items) {
        ItemStack[] cloned = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            cloned[i] = items[i] != null ? items[i].clone() : null;
        }
        return cloned;
    }
}
