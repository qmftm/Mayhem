package me.qmftm.asurajang.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerGoldRewardEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int amount;
    private final Component multiKillLabel;
    private final List<String> bonusReasons;

    public PlayerGoldRewardEvent(Player player, int amount, Component multiKillLabel, List<String> bonusReasons) {
        this.player       = player;
        this.amount       = amount;
        this.multiKillLabel = multiKillLabel;
        this.bonusReasons = bonusReasons;
    }

    public Player getPlayer()           { return player; }
    public int getAmount()              { return amount; }
    public Component getMultiKillLabel(){ return multiKillLabel; }
    public List<String> getBonusReasons(){ return bonusReasons; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList()          { return HANDLERS; }
}
