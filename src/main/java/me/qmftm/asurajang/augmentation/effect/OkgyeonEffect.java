package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.HandlerList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class OkgyeonEffect implements AugmentationEffect, Listener {

    private UUID snowyUuid;
    private UUID blackUuid;
    private BukkitTask snowyRespawnTask;
    private BukkitTask blackRespawnTask;
    private Player owner;

    @Override
    public void onActivate(Player player) {
        this.owner = player;
        Asurajang.getInstance().getServer().getPluginManager()
            .registerEvents(this, Asurajang.getInstance());
        spawnSnowy();
        spawnBlack();
    }

    @Override
    public void onDeactivate(Player player) {
        HandlerList.unregisterAll(this);
        if (snowyRespawnTask != null) { snowyRespawnTask.cancel(); snowyRespawnTask = null; }
        if (blackRespawnTask != null) { blackRespawnTask.cancel(); blackRespawnTask = null; }
        removeWolf(snowyUuid);
        removeWolf(blackUuid);
        snowyUuid = null;
        blackUuid = null;
    }

    @EventHandler
    public void onWolfDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        UUID dead = wolf.getUniqueId();

        if (dead.equals(snowyUuid)) {
            snowyUuid = null;
            if (snowyRespawnTask != null) snowyRespawnTask.cancel();
            snowyRespawnTask = Asurajang.getInstance().getServer().getScheduler()
                .runTaskLater(Asurajang.getInstance(), () -> {
                    if (owner.isOnline() && Asurajang.getInstance().getGameManager().isRunning()) {
                        spawnSnowy();
                    }
                }, 60 * 20L);
        } else if (dead.equals(blackUuid)) {
            blackUuid = null;
            if (blackRespawnTask != null) blackRespawnTask.cancel();
            blackRespawnTask = Asurajang.getInstance().getServer().getScheduler()
                .runTaskLater(Asurajang.getInstance(), () -> {
                    if (owner.isOnline() && Asurajang.getInstance().getGameManager().isRunning()) {
                        spawnBlack();
                    }
                }, 60 * 20L);
        }
    }

    private void spawnSnowy() {
        Wolf wolf = spawnWolf(Wolf.Variant.SNOWY, owner.getName() + "의 옥견 백",
            getCollarColor(Wolf.Variant.SNOWY));
        snowyUuid = wolf.getUniqueId();
    }

    private void spawnBlack() {
        Wolf wolf = spawnWolf(Wolf.Variant.BLACK, owner.getName() + "의 옥견 흑",
            getCollarColor(Wolf.Variant.BLACK));
        blackUuid = wolf.getUniqueId();
    }

    private Wolf spawnWolf(Wolf.Variant variant, String name, DyeColor collarColor) {
        return owner.getWorld().spawn(owner.getLocation(), Wolf.class, w -> {
            w.setVariant(variant);
            w.setTamed(true);
            w.setOwner(owner);
            w.customName(Component.text(name, NamedTextColor.WHITE));
            w.setCustomNameVisible(true);
            w.setCollarColor(collarColor);
            w.setAngry(false);
        });
    }

    private DyeColor getCollarColor(Wolf.Variant variant) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        if (gm.getGameMode() == GameManager.GameMode.TEAM) {
            int team = gm.getTeam(owner.getUniqueId());
            return team == 0 ? DyeColor.RED : DyeColor.BLUE;
        }
        return variant == Wolf.Variant.SNOWY ? DyeColor.WHITE : DyeColor.BLACK;
    }

    private void removeWolf(UUID uuid) {
        if (uuid == null) return;
        var entity = Asurajang.getInstance().getServer().getEntity(uuid);
        if (entity != null) entity.remove();
    }
}
