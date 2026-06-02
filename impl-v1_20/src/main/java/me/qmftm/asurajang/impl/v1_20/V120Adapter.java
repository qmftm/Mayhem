package me.qmftm.asurajang.impl.v1_20;

import me.qmftm.asurajang.api.VersionAdapter;
import org.bukkit.entity.Player;

public class V120Adapter implements VersionAdapter {

    @Override
    public void respawnPlayer(Player player) {
        player.spigot().respawn();
    }

    @Override
    public String getVersionLabel() {
        return "1.20~1.21";
    }
}
