package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public interface AugmentationEffect {
    void onActivate(Player player);
    void onDeactivate(Player player);

    default void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {}
    default void onProjectileDamageAsAttacker(Player shooter, EntityDamageByEntityEvent event) {}
    default void onKillEnemy(Player player, Player victim) {}
    default void onRightClick(Player player, PlayerInteractEvent event) {}
    default void onInteractEntity(Player player, PlayerInteractEntityEvent event) {}
    default void onSwapHands(Player player, PlayerSwapHandItemsEvent event) {}
    default void onToggleFlight(Player player, PlayerToggleFlightEvent event) {}
    default void onDropItem(Player player, PlayerDropItemEvent event) {}
    default void onRegainHealth(Player player, EntityRegainHealthEvent event) {}
    default void onOwnerDeath(Player player) {}
    default void onOwnerRespawn(Player player) {}
    default MaxHealthModifier getMaxHealthModifier() { return null; }
    default double getRespawnDelayMultiplier() { return 1.0; }
}
