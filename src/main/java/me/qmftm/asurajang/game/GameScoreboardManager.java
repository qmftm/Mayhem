package me.qmftm.asurajang.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameScoreboardManager {

    // 각 줄의 고유 식별자 (§ 색상코드)
    private static final String[] KEYS = {"§0", "§1", "§2", "§3", "§4", "§5"};

    private final Map<UUID, Scoreboard> boards  = new HashMap<>();
    private final Map<UUID, Integer>    kills   = new HashMap<>();
    private final Map<UUID, Integer>    gold    = new HashMap<>();
    private final Map<UUID, Integer>    levels  = new HashMap<>();

    // ── 설정 / 해제 ─────────────────────────────────────────────────────────

    public void setup(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY,
            Component.text("아수라장", NamedTextColor.GOLD));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < KEYS.length; i++) {
            obj.getScore(KEYS[i]).setScore(KEYS.length - i);
            Team team = board.registerNewTeam("line" + i);
            team.addEntry(KEYS[i]);
        }

        boards.put(player.getUniqueId(), board);
        kills.putIfAbsent(player.getUniqueId(), 0);
        gold.putIfAbsent(player.getUniqueId(), 0);
        levels.putIfAbsent(player.getUniqueId(), 0);

        player.setScoreboard(board);
        updatePlayer(player, 0);
    }

    public void remove(Player player) {
        boards.remove(player.getUniqueId());
        kills.remove(player.getUniqueId());
        gold.remove(player.getUniqueId());
        levels.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void removeAll() {
        for (UUID uuid : new ArrayList<>(boards.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) remove(p);
        }
        boards.clear();
        kills.clear();
        gold.clear();
        levels.clear();
    }

    // ── 갱신 ────────────────────────────────────────────────────────────────

    public void updatePlayer(Player player, int remainingSeconds) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        int k   = kills.getOrDefault(player.getUniqueId(), 0);
        int g   = gold.getOrDefault(player.getUniqueId(), 0);
        int lvl = levels.getOrDefault(player.getUniqueId(), 0);

        String[] lines = {
            "",
            "골드: "      + g,
            "킬: "        + k,
            "레벨: "      + lvl,
            "남은 시간: " + formatTime(remainingSeconds),
            ""
        };

        for (int i = 0; i < lines.length; i++) {
            Team team = board.getTeam("line" + i);
            if (team != null) {
                team.prefix(Component.text(lines[i], NamedTextColor.WHITE));
            }
        }
    }

    public void updateAll(int remainingSeconds) {
        for (UUID uuid : boards.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) updatePlayer(p, remainingSeconds);
        }
    }

    // ── 통계 ────────────────────────────────────────────────────────────────

    public void addKill(Player player) {
        kills.merge(player.getUniqueId(), 1, Integer::sum);
    }

    public void addGold(Player player, int amount) {
        gold.merge(player.getUniqueId(), amount, Integer::sum);
    }

    public int getGold(Player player) {
        return gold.getOrDefault(player.getUniqueId(), 0);
    }

    public void setLevel(Player player, int level) {
        levels.put(player.getUniqueId(), level);
    }

    public int getLevel(Player player) {
        return levels.getOrDefault(player.getUniqueId(), 0);
    }

    // ── 팀 ──────────────────────────────────────────────────────────────────

    // 각 플레이어의 개인 스코어보드에 레드/블루 팀을 등록하고 이름 색상·접두사 적용
    public void setupGameTeams(Map<UUID, Integer> teamMap) {
        for (Scoreboard board : boards.values()) {
            Team red = board.registerNewTeam("mayhem_red");
            red.color(NamedTextColor.RED);
            red.prefix(Component.text("[레드] ", NamedTextColor.RED));

            Team blue = board.registerNewTeam("mayhem_blue");
            blue.color(NamedTextColor.BLUE);
            blue.prefix(Component.text("[블루] ", NamedTextColor.BLUE));

            for (Map.Entry<UUID, Integer> e : teamMap.entrySet()) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p == null) continue;
                (e.getValue() == 0 ? red : blue).addEntry(p.getName());
            }
        }
    }

    public void cleanupGameTeams() {
        for (Scoreboard board : boards.values()) {
            Team red = board.getTeam("mayhem_red");
            if (red != null) red.unregister();
            Team blue = board.getTeam("mayhem_blue");
            if (blue != null) blue.unregister();
        }
    }

    // ── 내부 ────────────────────────────────────────────────────────────────

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
