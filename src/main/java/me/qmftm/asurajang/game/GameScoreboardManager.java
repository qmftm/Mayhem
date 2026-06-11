package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameScoreboardManager {

    private static final String[] KEYS = {"§0", "§1", "§2", "§3", "§4", "§5", "§6"};

    private final Map<UUID, Scoreboard> boards   = new HashMap<>();
    private final Map<UUID, Integer>    kills    = new HashMap<>();
    private final Map<UUID, Integer>    deaths   = new HashMap<>();
    private final Map<UUID, Integer>    assists  = new HashMap<>();
    private final Map<UUID, Integer>    gold     = new HashMap<>();
    private final Map<UUID, Integer>    levels   = new HashMap<>();
    private final Map<UUID, Integer>    exp      = new HashMap<>();

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

        Objective hpObj = board.registerNewObjective("asr_hp", Criteria.DUMMY,
            Component.text("❤", NamedTextColor.RED));
        hpObj.setDisplaySlot(DisplaySlot.BELOW_NAME);

        // 기존 플레이어들의 체력을 새 보드에 등록
        for (UUID existingUuid : boards.keySet()) {
            Player existing = Bukkit.getPlayer(existingUuid);
            if (existing != null) {
                hpObj.getScore(existing.getName()).setScore((int) Math.round(existing.getHealth()));
            }
        }

        kills.putIfAbsent(player.getUniqueId(), 0);
        deaths.putIfAbsent(player.getUniqueId(), 0);
        assists.putIfAbsent(player.getUniqueId(), 0);
        gold.putIfAbsent(player.getUniqueId(), 200);
        levels.putIfAbsent(player.getUniqueId(), 0);
        exp.putIfAbsent(player.getUniqueId(), 0);

        Asurajang.getInstance().getMaxHealthManager().setup(player.getUniqueId());
        boards.put(player.getUniqueId(), board);

        // 새 플레이어의 체력을 모든 보드(자신 포함)에 등록
        syncHealthOnAllBoards(player);

        player.setScoreboard(board);
        updatePlayer(player, 0);
    }

    public void remove(Player player) {
        Asurajang.getInstance().getStatAnvilListener().cleanup(player);
        Asurajang.getInstance().getLevelUpManager().cleanup(player.getUniqueId());
        Asurajang.getInstance().getPrismAugItemListener().cleanup(player.getUniqueId());
        Asurajang.getInstance().getMaxHealthManager().remove(player.getUniqueId());
        // 이 플레이어의 체력 점수를 다른 보드에서 제거
        boards.remove(player.getUniqueId());
        for (Scoreboard board : boards.values()) {
            board.resetScores(player.getName());
        }

        kills.remove(player.getUniqueId());
        deaths.remove(player.getUniqueId());
        assists.remove(player.getUniqueId());
        gold.remove(player.getUniqueId());
        levels.remove(player.getUniqueId());
        exp.remove(player.getUniqueId());
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
        exp.clear();
    }

    // ── 갱신 ────────────────────────────────────────────────────────────────

    public void updatePlayer(Player player, int remainingSeconds) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        int k       = kills.getOrDefault(player.getUniqueId(), 0);
        int d       = deaths.getOrDefault(player.getUniqueId(), 0);
        int a       = assists.getOrDefault(player.getUniqueId(), 0);
        int g       = gold.getOrDefault(player.getUniqueId(), 0);
        int lvl     = levels.getOrDefault(player.getUniqueId(), 0);
        int curExp  = exp.getOrDefault(player.getUniqueId(), 0);
        int reqExp  = expRequired(lvl);

        Component[] lines = {
            Component.empty(),
            Component.text("시간  ", NamedTextColor.WHITE)
                .append(Component.text(formatTime(remainingSeconds), NamedTextColor.AQUA)),
            Component.text("스코어  ", NamedTextColor.WHITE)
                .append(Component.text(String.valueOf(k), NamedTextColor.GREEN))
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(d), NamedTextColor.RED))
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(a), NamedTextColor.AQUA)),
            Component.text("레벨  ", NamedTextColor.WHITE)
                .append(Component.text(lvl + " Lv", NamedTextColor.GREEN)),
            lvl >= 15
                ? Component.text("경험치  ", NamedTextColor.WHITE)
                    .append(Component.text("MAX", NamedTextColor.GOLD))
                : Component.text("경험치  ", NamedTextColor.WHITE)
                    .append(Component.text(curExp + "/" + reqExp + " Exp", NamedTextColor.DARK_GREEN)),
            Component.text("골드  ", NamedTextColor.WHITE)
                .append(Component.text(g + " G", NamedTextColor.GOLD)),
            Component.empty()
        };

        for (int i = 0; i < lines.length; i++) {
            Team team = board.getTeam("line" + i);
            if (team != null) team.prefix(lines[i]);
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
        int k = kills.getOrDefault(player.getUniqueId(), 0);
        int d = deaths.getOrDefault(player.getUniqueId(), 0);
        int a = assists.getOrDefault(player.getUniqueId(), 0);

        GameManager gm = Asurajang.getInstance().getGameManager();
        NamedTextColor nameColor;
        if (gm.getGameMode() == GameManager.GameMode.TEAM) {
            int team = gm.getTeam(player.getUniqueId());
            nameColor = team == 0 ? NamedTextColor.RED : NamedTextColor.BLUE;
        } else {
            nameColor = NamedTextColor.WHITE;
        }

        player.playerListName(Component.text()
            .append(Component.text(player.getName(), nameColor))
            .append(Component.text("  " + k + "/" + d + "/" + a, NamedTextColor.GRAY))
            .build());

        syncHealthOnAllBoards(player);
    }

    // ── 체력 동기화 ──────────────────────────────────────────────────────────

    public void syncHealthOnAllBoards(Player player) {
        int hp = (int) Math.round(player.getHealth());
        for (Scoreboard board : boards.values()) {
            Objective obj = board.getObjective("asr_hp");
            if (obj != null) obj.getScore(player.getName()).setScore(hp);
        }
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

    // 현재 골드의 ratio 비율만큼 차감하고 실제 차감된 양을 반환
    public int removeGoldPercent(Player player, double ratio) {
        int current = getGold(player);
        int penalty = (int) Math.floor(current * ratio);
        if (penalty <= 0) return 0;
        gold.merge(player.getUniqueId(), -penalty, Integer::sum);
        return penalty;
    }

    public record ExpResult(int newLevel, boolean leveledUp) {}

    public ExpResult addExp(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int lvl = levels.getOrDefault(uuid, 0);
        if (lvl >= 15) return new ExpResult(lvl, false);
        int curExp = exp.merge(uuid, amount, Integer::sum);
        boolean leveled = false;
        while (lvl < 15 && curExp >= expRequired(lvl)) {
            curExp -= expRequired(lvl);
            lvl++;
            leveled = true;
        }
        if (lvl >= 15) curExp = 0;
        exp.put(uuid, curExp);
        levels.put(uuid, lvl);
        if (leveled) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) Asurajang.getInstance().getMaxHealthManager().recalculate(p);
        }
        return new ExpResult(lvl, leveled);
    }

    public void setLevel(Player player, int level) {
        levels.put(player.getUniqueId(), level);
    }

    public int getLevel(Player player) {
        return levels.getOrDefault(player.getUniqueId(), 0);
    }

    // 현재 레벨 → 다음 레벨까지 필요 경험치: (다음 레벨)^2 * 5 + 50
    private static int expRequired(int level) {
        return (level + 1) * (level + 1) * 5 + 50;
    }

    // ── 팀 ──────────────────────────────────────────────────────────────────

    public void setupGameTeams(Map<UUID, Integer> teamMap) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();

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

    // ── 엔티티 팀 등록 ───────────────────────────────────────────────────────

    public void addEntityToOwnerTeam(Entity entity, UUID ownerUuid) {
        int ownerTeam = Asurajang.getInstance().getGameManager().getTeam(ownerUuid);
        if (ownerTeam == -1) return;
        String teamName = ownerTeam == 0 ? "mayhem_red" : "mayhem_blue";
        String entry = entity.getUniqueId().toString();

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        Team t = main.getTeam(teamName);
        if (t != null) t.addEntry(entry);

        for (Scoreboard board : boards.values()) {
            Team bt = board.getTeam(teamName);
            if (bt != null) bt.addEntry(entry);
        }
    }

    public void removeEntityFromTeams(Entity entity) {
        String entry = entity.getUniqueId().toString();

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team t : main.getTeams()) t.removeEntry(entry);

        for (Scoreboard board : boards.values()) {
            for (Team t : board.getTeams()) t.removeEntry(entry);
        }
    }

    // ── 내부 ────────────────────────────────────────────────────────────────

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
