package me.qmftm.asurajang.impl.v26;

import me.qmftm.asurajang.api.VersionAdapter;
import org.bukkit.entity.Player;

public class V26Adapter implements VersionAdapter {

    @Override
    public void respawnPlayer(Player player) {
        player.spigot().respawn();
    }

    @Override
    public String getVersionLabel() {
        return "26+";
    }
}
