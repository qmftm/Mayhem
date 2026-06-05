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

    private final Map<UUID, Scoreboard> boards   = new HashMap<>();
    private final Map<UUID, Integer>    kills    = new HashMap<>();
    private final Map<UUID, Integer>    deaths   = new HashMap<>();
    private final Map<UUID, Integer>    assists  = new HashMap<>();
    private final Map<UUID, Integer>    gold     = new HashMap<>();
    private final Map<UUID, Integer>    levels   = new HashMap<>();

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
        deaths.putIfAbsent(player.getUniqueId(), 0);
        assists.putIfAbsent(player.getUniqueId(), 0);
        gold.putIfAbsent(player.getUniqueId(), 0);
        levels.putIfAbsent(player.getUniqueId(), 0);

        player.setScoreboard(board);
        updatePlayer(player, 0);
    }

    public void remove(Player player) {
        boards.remove(player.getUniqueId());
        kills.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());
        assists.remove(player.getUniqueId());
        gold.remove(player.getUniqueId());
        levels.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.playerListName(null);
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
        deaths.clear();
        assists.clear();
        gold.clear();
        levels.clear();
    }

    // ── 갱신 ────────────────────────────────────────────────────────────────

    public void updatePlayer(Player player, int remainingSeconds) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        int k   = kills.getOrDefault(player.getUniqueId(), 0);
        int d   = deaths.getOrDefault(player.getUniqueId(), 0);
        int a   = assists.getOrDefault(player.getUniqueId(), 0);
        int g   = gold.getOrDefault(player.getUniqueId(), 0);
        int lvl = levels.getOrDefault(player.getUniqueId(), 0);

        Component[] lines = {
            Component.empty(),
            Component.text("시간  ", NamedTextColor.WHITE)
                .append(Component.text(formatTime(remainingSeconds), NamedTextColor.AQUA)),
            Component.text(String.valueOf(k), NamedTextColor.GREEN)
                .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
                .append(Component.text(String.valueOf(d), NamedTextColor.RED))
                .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
                .append(Component.text(String.valueOf(a), NamedTextColor.AQUA)),
            Component.text("레벨  ", NamedTextColor.WHITE)
                .append(Component.text(String.valueOf(lvl) + " Lv", NamedTextColor.GREEN)),
            Component.text("골드  ", NamedTextColor.WHITE)
                .append(Component.text(g + " G", NamedTextColor.GOLD)),
            Component.empty()
        };

        for (int i = 0; i < lines.length; i++) {
            Team team = board.getTeam("line" + i);
            if (team != null) {
                team.prefix(lines[i]);
            }
        }

        updateTabListEntry(player);
    }

    public void updateAll(int remainingSeconds) {
        for (UUID uuid : boards.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) updatePlayer(p, remainingSeconds);
        }
    }

    public void updateTabListEntry(Player player) {
        int k  = kills.getOrDefault(player.getUniqueId(), 0);
        int d  = deaths.getOrDefault(player.getUniqueId(), 0);
        int a  = assists.getOrDefault(player.getUniqueId(), 0);
        double hp = player.getHealth();

        player.playerListName(Component.text()
            .append(player.displayName())
            .append(Component.text("  " + k + "/" + d + "/" + a, NamedTextColor.GRAY))
            .appendNewline()
            .append(Component.text("❤ " + String.format("%.1f", hp), NamedTextColor.RED))
            .build());
    }

    // ── 통계 ────────────────────────────────────────────────────────────────

    public void addKill(Player player) {
        kills.merge(player.getUniqueId(), 1, Integer::sum);
        updateTabListEntry(player);
    }

    public void addDeath(Player player) {
        deaths.merge(player.getUniqueId(), 1, Integer::sum);
        updateTabListEntry(player);
    }

    public void addAssist(Player player) {
        assists.merge(player.getUniqueId(), 1, Integer::sum);
        updateTabListEntry(player);
    }

    public int getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0);
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

    // 메인 스코어보드(/team 호환) + 개인 스코어보드(이름색 표시) 양쪽에 팀 등록
    public void setupGameTeams(Map<UUID, Integer> teamMap) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();

        // 기존 팀이 남아있으면 정리
        Team prev;
        if ((prev = main.getTeam("mayhem_red"))  != null) prev.unregister();
        if ((prev = main.getTeam("mayhem_blue")) != null) prev.unregister();

        Team mainRed  = main.registerNewTeam("mayhem_red");
        mainRed.color(NamedTextColor.RED);
        mainRed.prefix(Component.text("[레드] ", NamedTextColor.RED));
        mainRed.setAllowFriendlyFire(false);

        Team mainBlue = main.registerNewTeam("mayhem_blue");
        mainBlue.color(NamedTextColor.BLUE);
        mainBlue.prefix(Component.text("[블루] ", NamedTextColor.BLUE));
        mainBlue.setAllowFriendlyFire(false);

        for (Map.Entry<UUID, Integer> e : teamMap.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) continue;
            (e.getValue() == 0 ? mainRed : mainBlue).addEntry(p.getName());
        }

        // 개인 스코어보드에도 동일하게 등록 (플레이어가 보는 이름 색상 적용)
        for (Scoreboard board : boards.values()) {
            Team red  = board.registerNewTeam("mayhem_red");
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
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        Team mainRed  = main.getTeam("mayhem_red");
        if (mainRed  != null) mainRed.unregister();
        Team mainBlue = main.getTeam("mayhem_blue");
        if (mainBlue != null) mainBlue.unregister();

        for (Scoreboard board : boards.values()) {
            Team red  = board.getTeam("mayhem_red");
            if (red  != null) red.unregister();
            Team blue = board.getTeam("mayhem_blue");
            if (blue != null) blue.unregister();
        }
    }

    // ── 내부 ────────────────────────────────────────────────────────────────

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
