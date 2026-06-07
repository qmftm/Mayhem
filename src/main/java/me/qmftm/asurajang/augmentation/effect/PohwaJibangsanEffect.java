package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PohwaJibangsanEffect implements AugmentationEffect, Listener {

    private static final int FIXED_FOOD_LEVEL = 19; // 9칸 반

    private Player owner;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        player.setFoodLevel(FIXED_FOOD_LEVEL);
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
        player.setFoodLevel(FIXED_FOOD_LEVEL);
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!event.getPlayer().equals(owner)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, false, false));
    }
}
