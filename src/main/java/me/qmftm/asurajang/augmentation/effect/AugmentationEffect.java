package me.qmftm.asurajang.augmentation.effect;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public interface AugmentationEffect {
    void onActivate(Player player);
    void onDeactivate(Player player);

    default void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {}
    default void onProjectileDamageAsAttacker(Player shooter, EntityDamageByEntityEvent event) {}
    default void onKillEnemy(Player player, Player victim) {}
    default void onRightClick(Player player, PlayerInteractEvent event) {}
    default void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {}
}
