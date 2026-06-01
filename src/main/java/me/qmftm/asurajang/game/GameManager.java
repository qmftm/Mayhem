package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private static final int GAME_DURATION = 600; // 10분

    public enum State { WAITING, STARTING, RUNNING }
    public enum GameMode { TEAM, SOLO }

    private State state = State.WAITING;
    private GameMode gameMode = GameMode.SOLO;
    private final Map<UUID, PlayerInventorySnapshot> snapshots = new HashMap<>();
    private final Map<UUID, Integer> teams = new HashMap<>();
    private int remainingSeconds = GAME_DURATION;
    private BukkitTask timerTask;

    public boolean start(GameMode mode) {
        if (state != State.WAITING) return false;
        state = State.STARTING;
        gameMode = mode;

        Asurajang plugin = Asurajang.getInstance();
        World world = Bukkit.getWorlds().get(0);

        Bukkit.broadcast(Component.text("[아수라장] 전장을 탐색하는 중...", NamedTextColor.GOLD));

        plugin.getBattlefieldManager().searchAsync(world, () -> {
            if (state != State.STARTING) return;
            String biomeName = plugin.getBattlefieldManager().getCurrentBiomeName();
            Bukkit.broadcast(Component.text("[아수라장] 전장 발견: " + biomeName, NamedTextColor.YELLOW));
            runCountdown(plugin, world, biomeName);
        });

        return true;
    }

    private void runCountdown(Asurajang plugin, World world, String biomeName) {
        for (int i = 5; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (state != State.STARTING) return;
                Title t = Title.title(
                    Component.text(String.valueOf(count), NamedTextColor.YELLOW),
                    Component.text(biomeName + " 전장", NamedTextColor.GRAY),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
                );
                float pitch = 0.6f + (5 - count) * 0.2f; // 숫자가 낮을수록 음이 높아짐
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.showTitle(t);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                });
            }, (long)(5 - count) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != State.STARTING) return;

            state = State.RUNNING;
            remainingSeconds = GAME_DURATION;

            BattlefieldManager bm = plugin.getBattlefieldManager();
            Location battlefield = bm.getCurrentLocation();
            if (battlefield == null) battlefield = world.getSpawnLocation().clone().add(0.5, 1, 0.5);
            final Location loc = battlefield;

            Title startTitle = Title.title(
                Component.text("시작!", NamedTextColor.GREEN),
                Component.text(biomeName + " 전장", NamedTextColor.AQUA),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
            );

            bm.applyBorder();
            world.setTime(1000);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            GameScoreboardManager sbm = plugin.getScoreboardManager();
            Collection<? extends Player> online = Bukkit.getOnlinePlayers();
            if (gameMode == GameMode.TEAM) assignTeams(online);
            for (Player p : online) {
                snapshots.put(p.getUniqueId(), new PlayerInventorySnapshot(p));
                DefaultKit.apply(p);
                Location spawn;
                if (gameMode == GameMode.TEAM) {
                    spawn = bm.getTeamCornerSpawn(teams.getOrDefault(p.getUniqueId(), 0));
                } else {
                    spawn = bm.getRandomSpawn();
                }
                p.teleport(spawn != null ? spawn : loc);
                p.showTitle(startTitle);
                sbm.setup(p);
            }

            String modeLabel = gameMode == GameMode.TEAM ? "팀전" : "개인전";
            Bukkit.broadcast(Component.text("[아수라장] 게임 시작! (" + modeLabel + ") — " + biomeName + " 전장", NamedTextColor.GOLD));
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
            });

            startTimer(plugin);
        }, 5 * 20L);
    }

    private void startTimer(Asurajang plugin) {
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingSeconds <= 0) {
                stop();
                return;
            }
            remainingSeconds--;
            plugin.getScoreboardManager().updateAll(remainingSeconds);
        }, 20L, 20L);
    }

    public boolean stop() {
        if (state == State.WAITING) return false;
        state = State.WAITING;

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        Bukkit.broadcast(Component.text("[아수라장] 게임이 종료되었습니다.", NamedTextColor.GOLD));

        Asurajang plugin = Asurajang.getInstance();
        plugin.getAugmentationManager().deactivateAll(Bukkit.getOnlinePlayers());
        plugin.getBattlefieldManager().resetBorder();
        plugin.getScoreboardManager().removeAll();
        Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventorySnapshot snapshot = snapshots.remove(player.getUniqueId());
            if (snapshot != null) snapshot.restore(player);
        }
        snapshots.clear();
        teams.clear();

        return true;
    }

    private void assignTeams(Collection<? extends Player> players) {
        teams.clear();
        List<Player> list = new ArrayList<>(players);
        Collections.shuffle(list);
        for (int i = 0; i < list.size(); i++) {
            teams.put(list.get(i).getUniqueId(), i % 2);
        }
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public State getState() {
        return state;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    // 0 = NW팀, 1 = SE팀, -1 = 개인전 또는 미배정
    public int getTeam(UUID uuid) {
        return teams.getOrDefault(uuid, -1);
    }
}
