package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PohwaJibangsanEffect implements AugmentationEffect, Listener {

    private Player owner;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        player.setFoodLevel(AugmentSettings.getInt("SaturatedFat", "fixed-food-level", 19));
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        owner = null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.equals(owner)) return;
        event.setCancelled(true);
        player.setFoodLevel(AugmentSettings.getInt("SaturatedFat", "fixed-food-level", 19));
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!event.getPlayer().equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, false, false));
    }
}
