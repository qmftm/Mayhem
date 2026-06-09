package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
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

    private static final int GAME_DURATION = 900; // 15분

    public enum State { WAITING, STARTING, RUNNING }
    public enum GameMode { TEAM, SOLO }
    public enum BaseMode { OFF, BASE, WILD }

    private State state = State.WAITING;
    private GameMode gameMode = GameMode.SOLO;
    private final Map<UUID, PlayerInventorySnapshot> snapshots = new HashMap<>();
    private final Map<UUID, Integer> teams = new HashMap<>();
    private int remainingSeconds = GAME_DURATION;
    private BukkitTask timerTask;
    private BukkitTask timeTask;
    private boolean firstBloodClaimed = false;

    // 게임이 끝나도 유지되는 설정
    private BaseMode baseMode = BaseMode.OFF;
    private boolean guardianAttackEnabled = true;

    public BaseMode getBaseMode() {
        return baseMode;
    }

    public void setBaseMode(BaseMode mode) {
        this.baseMode = mode;
    }

    public boolean isBaseModeEnabled() {
        return baseMode == BaseMode.BASE;
    }

    public boolean isGuardianAttackEnabled() {
        return guardianAttackEnabled;
    }

    public void setGuardianAttackEnabled(boolean enabled) {
        this.guardianAttackEnabled = enabled;
    }

    public boolean start(GameMode mode, Player sender) {
        if (state != State.WAITING) return false;
        state = State.STARTING;
        gameMode = mode;

        Asurajang plugin = Asurajang.getInstance();
        World world = Bukkit.getWorlds().get(0);

        sender.sendMessage(Component.text("[아수라장] 전장을 탐색하는 중...", NamedTextColor.GOLD));

        final int[] tick = {0};
        final String[] DOTS = {"", ".", "..", "..."};
        final BukkitTask[] animTask = {null};
        animTask[0] = Bukkit.getScheduler().runTaskTimer(plugin, () ->
            sender.sendActionBar(Component.text("탐색 중" + DOTS[tick[0]++ % DOTS.length], NamedTextColor.YELLOW)),
        0L, 8L);

        plugin.getBattlefieldManager().searchAsync(world, () -> {
            animTask[0].cancel();
            sender.sendActionBar(Component.empty());
            if (state != State.STARTING) return;
            String biomeName = plugin.getBattlefieldManager().getCurrentBiomeName();
            runSlotMachine(plugin, biomeName, () -> runCountdown(plugin, world, biomeName));
        });

        return true;
    }

    private void runSlotMachine(Asurajang plugin, String targetName, Runnable onFinish) {
        List<String> pool = new ArrayList<>(BattlefieldManager.getAllBiomeNames());
        pool.remove(targetName);
        Collections.shuffle(pool);

        int[][] phases  = {{2, 6}, {4, 4}, {6, 3}, {9, 2}};
        float[] pitches = {1.9f,  1.4f,  1.0f,  0.7f};
        long tick = 0;
        int poolIdx = 0;

        for (int phaseIdx = 0; phaseIdx < phases.length; phaseIdx++) {
            int interval = phases[phaseIdx][0], count = phases[phaseIdx][1];
            final float pitch = pitches[phaseIdx];
            for (int i = 0; i < count; i++) {
                tick += interval;
                final String name = pool.get(poolIdx++ % pool.size());
                final long delay = tick;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (state != State.STARTING) return;
                    NamedTextColor color = BattlefieldManager.getBiomeColor(name);
                    Title frame = Title.title(
                        Component.text(name, color),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(300), Duration.ZERO)
                    );
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.showTitle(frame);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, pitch);
                    });
                }, delay);
            }
        }

        // 최종 공개
        tick += 15;
        final long revealAt = tick;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != State.STARTING) return;
            showBracketTitle(targetName, true);
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.0f);
            });
        }, revealAt);

        // 대괄호 깜빡임 4회 (ON/OFF × 4 = 8 상태 전환, 10틱 간격)
        for (int b = 0; b < 8; b++) {
            final long blinkDelay = revealAt + 8 + b * 7L;
            final boolean on = (b % 2 == 0);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (state != State.STARTING) return;
                if (on) {
                    showBracketTitle(targetName, false);
                    Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f));
                } else {
                    NamedTextColor color = BattlefieldManager.getBiomeColor(targetName);
                    Title noBrackets = Title.title(
                        Component.text(targetName, color),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(600), Duration.ZERO)
                    );
                    Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(noBrackets));
                }
            }, blinkDelay);
        }

        // 깜빡임 후 대괄호 고정
        final long holdAt = revealAt + 8 + 56L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != State.STARTING) return;
            showBracketTitle(targetName, false);
        }, holdAt);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != State.STARTING) return;
            onFinish.run();
        }, holdAt + 12L);
    }

    private static void showBracketTitle(String name, boolean withSubtitle) {
        NamedTextColor color = BattlefieldManager.getBiomeColor(name);
        Component titleComp = Component.text("[", NamedTextColor.WHITE)
            .append(Component.text(name, color))
            .append(Component.text("]", NamedTextColor.WHITE));
        Component subtitle = withSubtitle
            ? Component.text("전장이 선택되었습니다!", NamedTextColor.GRAY)
            : Component.empty();
        Title title = Title.title(titleComp, subtitle,
            Title.Times.times(Duration.ZERO, Duration.ofMillis(700), Duration.ZERO));
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(title));
    }

    private void runCountdown(Asurajang plugin, World world, String biomeName) {
        for (int i = 3; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (state != State.STARTING) return;
                Title t = Title.title(
                    Component.text(String.valueOf(count), NamedTextColor.YELLOW),
                    Component.text(biomeName + " 전장", NamedTextColor.GRAY),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
                );
                float pitch = 0.6f + (3 - count) * 0.2f; // 숫자가 낮을수록 음이 높아짐
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.showTitle(t);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                });
            }, (long)(3 - count) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != State.STARTING) return;

            state = State.RUNNING;
            remainingSeconds = (baseMode == BaseMode.WILD) ? NexusSettings.wildDurationSeconds() : GAME_DURATION;
            firstBloodClaimed = false;

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
            if (baseMode == BaseMode.WILD) bm.startBorderShrink(remainingSeconds);
            world.setTime(1000);
            timeTask = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> world.setTime(1000), 20L, 20L);

            GameScoreboardManager sbm = plugin.getScoreboardManager();
            Collection<? extends Player> online = Bukkit.getOnlinePlayers();
            if (gameMode == GameMode.TEAM) assignTeams(online);
            for (Player p : online) {
                snapshots.put(p.getUniqueId(), new PlayerInventorySnapshot(p));
                DefaultKit.apply(p);
                Location spawn;
                if (gameMode == GameMode.TEAM && baseMode == BaseMode.BASE) {
                    spawn = bm.getTeamCornerSpawn(teams.getOrDefault(p.getUniqueId(), 0), true);
                } else if (gameMode == GameMode.TEAM) {
                    spawn = bm.getTeamRandomSpawn(teams.getOrDefault(p.getUniqueId(), 0));
                } else {
                    spawn = bm.getRandomSpawn();
                }
                p.teleport(spawn != null ? spawn : loc);
                p.showTitle(startTitle);
                sbm.setup(p);
            }
            if (gameMode == GameMode.TEAM) {
                sbm.setupGameTeams(teams);
                for (Player p : online) {
                    int t = teams.getOrDefault(p.getUniqueId(), 0);
                    p.sendMessage(Component.text("당신은 ")
                        .append(t == 0
                            ? Component.text("레드팀", NamedTextColor.RED)
                            : Component.text("블루팀", NamedTextColor.BLUE))
                        .append(Component.text("입니다!")));
                }
            }

            String modeLabel = gameMode == GameMode.TEAM ? "팀전" : "개인전";
            Bukkit.broadcast(Component.text("[아수라장] 게임 시작! (" + modeLabel + ") — " + biomeName + " 전장", NamedTextColor.GOLD));
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
            });

            startTimer(plugin);
        }, 3 * 20L);
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
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }

        Bukkit.broadcast(Component.text("[아수라장] 게임이 종료되었습니다.", NamedTextColor.GOLD));

        Asurajang plugin = Asurajang.getInstance();
        plugin.getAugmentationManager().deactivateAll(Bukkit.getOnlinePlayers());
        plugin.getBattlefieldManager().resetBorder();
        plugin.getScoreboardManager().cleanupGameTeams();
        plugin.getScoreboardManager().removeAll();

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

    // 경기 중 첫 킬이면 true를 반환하고 이후로는 false (선취점)
    public boolean claimFirstBlood() {
        if (firstBloodClaimed) return false;
        firstBloodClaimed = true;
        return true;
    }
}
