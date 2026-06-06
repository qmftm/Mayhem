package me.qmftm.asurajang.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerExpRewardEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int amount;
    private final int newLevel;
    private final boolean leveledUp;

    public PlayerExpRewardEvent(Player player, int amount, int newLevel, boolean leveledUp) {
        this.player    = player;
        this.amount    = amount;
        this.newLevel  = newLevel;
        this.leveledUp = leveledUp;
    }

    public Player getPlayer()   { return player; }
    public int getAmount()      { return amount; }
    public int getNewLevel()    { return newLevel; }
    public boolean isLeveledUp(){ return leveledUp; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList()          { return HANDLERS; }
}
