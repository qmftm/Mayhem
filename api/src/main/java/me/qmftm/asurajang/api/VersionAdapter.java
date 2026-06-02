package me.qmftm.asurajang.api;

import org.bukkit.entity.Player;

public interface VersionAdapter {
    /** 클라이언트 리스폰 강제 호출 */
    void respawnPlayer(Player player);

    /** 로깅용 버전 레이블 */
    String getVersionLabel();
}
