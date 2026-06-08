package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BattlefieldManager implements Listener {

    private static final int CHUNK_SIZE     = 16;
    private static final int BORDER_CHUNKS  = 6;
    private static final double BORDER_SIZE   = BORDER_CHUNKS * CHUNK_SIZE; // 96 (6x6 청크)
    private static final double BORDER_RADIUS = BORDER_SIZE / 2.0;
    private static final int HISTORY_SIZE  = 3;
    private static final int LOCATE_RADIUS     = 25600;
    private static final int LOCATE_STEP_HORIZ = 32;
    private static final int LOCATE_STEP_VERT  = 64;
    private static final int BIOME_CHECK_MARGIN_CHUNKS = 1; // 모서리에서 안쪽으로 바이옴을 검사할 청크 수
    private static final int MAX_SEARCH_ATTEMPTS       = 6; // 모서리 바이옴 불일치 시 재탐색 최대 횟수
    private static final int MAX_BASE_HEIGHT_DIFF      = 12; // 기지 모드에서 양 팀 코너 간 허용 지면 높이 차 (이보다 크면 재탐색)
    private static final int MAX_RAMP_STEPS            = 16; // 기지가 지면보다 높이 솟았을 때 만들어줄 진입 계단의 최대 길이

    private static final List<Biome> BIOME_POOL = List.of(
        Biome.PLAINS,          Biome.CHERRY_GROVE,
        Biome.DESERT,          Biome.SNOWY_PLAINS,
        Biome.FROZEN_PEAKS,    Biome.BADLANDS,
        Biome.MEADOW,          Biome.WINDSWEPT_HILLS,
        Biome.SUNFLOWER_PLAINS, Biome.STONY_PEAKS
    );

    private static final Map<Biome, String> BIOME_NAMES = Map.of(
        Biome.PLAINS,           "평원",
        Biome.CHERRY_GROVE,     "벚꽃 숲",
        Biome.DESERT,           "사막",
        Biome.SNOWY_PLAINS,     "눈 덮인 평원",
        Biome.FROZEN_PEAKS,     "역고드름",
        Biome.BADLANDS,         "메사",
        Biome.MEADOW,           "목초지",
        Biome.WINDSWEPT_HILLS,  "바람이 세찬 언덕",
        Biome.SUNFLOWER_PLAINS, "해바라기 평원",
        Biome.STONY_PEAKS,      "바위 봉우리"
    );

    private static final Map<String, NamedTextColor> BIOME_COLORS = Map.of(
        "평원",             NamedTextColor.GREEN,
        "벚꽃 숲",          NamedTextColor.LIGHT_PURPLE,
        "사막",             NamedTextColor.GOLD,
        "눈 덮인 평원",     NamedTextColor.AQUA,
        "역고드름",         NamedTextColor.WHITE,
        "메사",             NamedTextColor.RED,
        "목초지",           NamedTextColor.DARK_GREEN,
        "바람이 세찬 언덕", NamedTextColor.DARK_GRAY,
        "해바라기 평원",    NamedTextColor.YELLOW,
        "바위 봉우리",      NamedTextColor.GRAY
    );

    private static final int BASE_SIZE = 3; // 3x1x3 기지 크기
    private static final int BEACON_HEIGHT = 3; // 콘크리트 거점 위 신호기 높이
    private static final int GUARDIAN_SLIME_SIZE = 3; // 거점 히트박스용 슬라임 크기
    // 라이프 개수·체력, 회복 시간, 어그로·투사체·공격 주기 등은 nexus.guardian 설정에서 읽어온다 (NexusSettings 참고)

    private static final class GuardianState {
        final BossBar bar;
        final String teamLabel;
        final NamedTextColor color;
        final BossBar.Color barColor;
        final int teamIndex;
        int lives;
        boolean recovering;
        @Nullable BukkitTask attackTask;
        int attackCooldownTicks;

        GuardianState(BossBar bar, String teamLabel, NamedTextColor color, BossBar.Color barColor, int teamIndex, int lives) {
            this.bar = bar;
            this.teamLabel = teamLabel;
            this.color = color;
            this.barColor = barColor;
            this.teamIndex = teamIndex;
            this.lives = lives;
        }
    }

    private final LinkedList<Biome> usedBiomes = new LinkedList<>();
    private final Map<Integer, Location> teamBaseSpawns = new HashMap<>();
    private final Map<UUID, GuardianState> guardianStates = new HashMap<>();
    private Integer sharedBaseY; // 양 팀 기지가 비슷한 높이에 놓이도록 캐시한 공통 기준 y

    private volatile Location currentLocation;
    private volatile String currentBiomeName = "";

    // ── 탐색 ────────────────────────────────────────────────────────────────

    public void searchAsync(World world, Runnable onComplete) {
        currentLocation = null;
        currentBiomeName = "";
        teamBaseSpawns.clear();
        clearBaseGuardians();
        sharedBaseY = null;

        Biome target = pickNext();
        currentBiomeName = BIOME_NAMES.getOrDefault(target, target.name());

        attemptSearch(world, target, 1, onComplete);
    }

    // 후보 위치를 찾고, 보더 영역 모서리의 바이옴이 목표와 다르면 재탐색
    private void attemptSearch(World world, Biome target, int attempt, Runnable onComplete) {
        Asurajang plugin = Asurajang.getInstance();

        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        double dist  = 2000 + ThreadLocalRandom.current().nextDouble() * 6000;
        Location randomOrigin = new Location(world,
            Math.cos(angle) * dist, 64, Math.sin(angle) * dist);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var result = world.locateNearestBiome(
                randomOrigin, LOCATE_RADIUS, LOCATE_STEP_HORIZ, LOCATE_STEP_VERT, target);
            Location found = result != null ? result.getLocation() : null;

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (found == null) {
                    onComplete.run();
                    return;
                }

                Location dry = findDryLocation(world, found);
                int chunkX = dry.getBlockX() >> 4;
                int chunkZ = dry.getBlockZ() >> 4;
                if (!world.isChunkLoaded(chunkX, chunkZ)) world.loadChunk(chunkX, chunkZ);
                int y = world.getHighestBlockYAt(dry.getBlockX(), dry.getBlockZ());
                Location candidate = new Location(world,
                    dry.getBlockX() + 0.5, y + 1, dry.getBlockZ() + 0.5);

                boolean biomeOk = isBorderEdgeBiomeConsistent(world, candidate, target);
                GameManager gm = Asurajang.getInstance().getGameManager();
                boolean baseModeActive = gm.isBaseModeEnabled() && gm.getGameMode() == GameManager.GameMode.TEAM;
                boolean baseHeightOk = !baseModeActive || isBaseHeightDifferenceAcceptable(world, candidate);

                if (attempt < MAX_SEARCH_ATTEMPTS && (!biomeOk || !baseHeightOk)) {
                    attemptSearch(world, target, attempt + 1, onComplete);
                    return;
                }

                currentLocation = candidate;
                onComplete.run();
            });
        });
    }

    // 후보 위치가 보더 중심이 됐을 때, 가장자리에서 BIOME_CHECK_MARGIN_CHUNKS 청크 안쪽인
    // 네 모서리 지점의 바이옴이 모두 목표 바이옴과 일치하는지 검사 (지형이 들쭉날쭉해지는 것 방지)
    private static boolean isBorderEdgeBiomeConsistent(World world, Location candidate, Biome target) {
        int chunkX = candidate.getBlockX() >> 4;
        int chunkZ = candidate.getBlockZ() >> 4;
        int centerX = chunkX * CHUNK_SIZE;
        int centerZ = chunkZ * CHUNK_SIZE;

        int half   = (int) BORDER_RADIUS;
        int margin = BIOME_CHECK_MARGIN_CHUNKS * CHUNK_SIZE;

        int[] xs = { centerX - half + margin, centerX + half - margin };
        int[] zs = { centerZ - half + margin, centerZ + half - margin };

        for (int x : xs) {
            for (int z : zs) {
                int chX = x >> 4, chZ = z >> 4;
                if (!world.isChunkLoaded(chX, chZ)) world.loadChunk(chX, chZ);
                int y = world.getHighestBlockYAt(x, z);
                if (world.getBiome(x, y, z) != target) return false;
            }
        }
        return true;
    }

    // 기지 모드일 때, 양 팀 기지가 들어설 두 코너의 지면 높이 차이가 너무 크면(평평하지 않은 지형)
    // 재탐색 대상으로 판단 (buildTeamBase의 코너 계산과 동일한 좌표 사용)
    private static boolean isBaseHeightDifferenceAcceptable(World world, Location candidate) {
        int offset = (int) (BORDER_RADIUS - 10);
        int cx0 = candidate.getBlockX() - offset;
        int cz0 = candidate.getBlockZ() - offset;
        int cx1 = candidate.getBlockX() + offset;
        int cz1 = candidate.getBlockZ() + offset;

        if (!world.isChunkLoaded(cx0 >> 4, cz0 >> 4)) world.loadChunk(cx0 >> 4, cz0 >> 4);
        if (!world.isChunkLoaded(cx1 >> 4, cz1 >> 4)) world.loadChunk(cx1 >> 4, cz1 >> 4);

        int groundY0 = findGroundY(world, cx0, cz0);
        int groundY1 = findGroundY(world, cx1, cz1);
        return Math.abs(groundY0 - groundY1) <= MAX_BASE_HEIGHT_DIFF;
    }

    // 수면/용암 위면 나선형으로 마른 땅을 탐색
    private static Location findDryLocation(World world, Location center) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        for (int radius = 0; radius <= 128; radius += 8) {
            int step = radius == 0 ? 1 : 8;
            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    if (radius > 0 && Math.abs(dx) < radius && Math.abs(dz) < radius) continue;
                    int x = cx + dx, z = cz + dz;
                    int chX = x >> 4, chZ = z >> 4;
                    if (!world.isChunkLoaded(chX, chZ)) world.loadChunk(chX, chZ);
                    int y = world.getHighestBlockYAt(x, z);
                    if (!isWet(world.getBlockAt(x, y, z).getType())) {
                        return new Location(world, x, y, z);
                    }
                }
            }
        }
        return center;
    }

    // 나뭇잎·눈처럼 발판으로 삼기 애매한 블록은 건너뛰고 실제 지면 y를 찾음
    private static int findGroundY(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        int min = world.getMinHeight();
        while (y > min && isFoliage(world.getBlockAt(x, y, z).getType())) {
            y--;
        }
        return y;
    }

    private static boolean isFoliage(Material mat) {
        String name = mat.name();
        return name.endsWith("_LEAVES")
            || mat == Material.SNOW
            || mat == Material.POWDER_SNOW
            || mat == Material.VINE
            || mat == Material.GLOW_LICHEN
            || mat == Material.HANGING_ROOTS;
    }

    private static boolean isWet(Material mat) {
        return switch (mat) {
            case WATER, LAVA, ICE, FROSTED_ICE, BLUE_ICE, PACKED_ICE -> true;
            default -> false;
        };
    }

    public static List<String> getAllBiomeNames() {
        List<String> names = new ArrayList<>();
        for (Biome b : BIOME_POOL) names.add(BIOME_NAMES.getOrDefault(b, b.name()));
        return names;
    }

    public static NamedTextColor getBiomeColor(String biomeName) {
        return BIOME_COLORS.getOrDefault(biomeName, NamedTextColor.WHITE);
    }

    // ── 월드 보더 ────────────────────────────────────────────────────────────

    // 발견된 위치가 속한 청크를 기준으로 4x4 청크 영역 전체가
    // 보더 안에 온전히 들어가도록 청크 경계에 정렬해 적용
    public void applyBorder() {
        if (currentLocation == null) return;
        World world = currentLocation.getWorld();

        int chunkX = currentLocation.getBlockX() >> 4;
        int chunkZ = currentLocation.getBlockZ() >> 4;
        int centerX = chunkX * CHUNK_SIZE;
        int centerZ = chunkZ * CHUNK_SIZE;

        if (!world.isChunkLoaded(centerX >> 4, centerZ >> 4)) world.loadChunk(centerX >> 4, centerZ >> 4);
        int y = world.getHighestBlockYAt(centerX, centerZ);
        currentLocation = new Location(world, centerX, y + 1, centerZ);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(BORDER_SIZE);
    }

    public void resetBorder() {
        Bukkit.getWorlds().get(0).getWorldBorder().reset();
        clearBaseGuardians();
    }

    // ── 랜덤 스폰 ────────────────────────────────────────────────────────────

    @Nullable
    public Location getRandomSpawn() {
        if (currentLocation == null) return null;
        World world = currentLocation.getWorld();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int margin = (int) (BORDER_RADIUS - 10);

        int x = currentLocation.getBlockX() + rng.nextInt(-margin, margin + 1);
        int z = currentLocation.getBlockZ() + rng.nextInt(-margin, margin + 1);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    @Nullable
    public Location getTeamCornerSpawn(int teamIndex, boolean buildBase) {
        if (currentLocation == null) return null;
        if (!buildBase) return randomTeamCornerSpawn(teamIndex);
        return teamBaseSpawns.computeIfAbsent(teamIndex, this::buildTeamBase);
    }

    private Location randomTeamCornerSpawn(int teamIndex) {
        World world = currentLocation.getWorld();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int offset = (int) (BORDER_RADIUS - 10);
        int sign = (teamIndex == 0) ? -1 : 1;

        int x = currentLocation.getBlockX() + sign * offset + rng.nextInt(-5, 6);
        int z = currentLocation.getBlockZ() + sign * offset + rng.nextInt(-5, 6);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    // 팀 코너에 3x1x3 콘크리트 기지를 깔고, 그 위 3블록 높이에 신호기를 띄운 뒤
    // 콘크리트 위를 스폰 지점으로 반환. 두 코너 중 더 높은 지면을 공통 기준 y로 사용해
    // 양 팀 기지가 비슷한 높이에 놓이면서도 낮은 쪽이 땅에 파묻히지 않도록 함
    private Location buildTeamBase(int teamIndex) {
        World world = currentLocation.getWorld();
        int offset = (int) (BORDER_RADIUS - 10);

        int cx0 = currentLocation.getBlockX() - offset;
        int cz0 = currentLocation.getBlockZ() - offset;
        int cx1 = currentLocation.getBlockX() + offset;
        int cz1 = currentLocation.getBlockZ() + offset;
        if (!world.isChunkLoaded(cx0 >> 4, cz0 >> 4)) world.loadChunk(cx0 >> 4, cz0 >> 4);
        if (!world.isChunkLoaded(cx1 >> 4, cz1 >> 4)) world.loadChunk(cx1 >> 4, cz1 >> 4);

        if (sharedBaseY == null) {
            int groundY0 = findGroundY(world, cx0, cz0);
            int groundY1 = findGroundY(world, cx1, cz1);
            // 더 높은 쪽 지면을 기준으로 삼아 낮은 쪽 기지가 땅에 파묻히지 않도록 함
            sharedBaseY = Math.max(groundY0, groundY1);
        }

        int sign = (teamIndex == 0) ? -1 : 1;
        int cx = currentLocation.getBlockX() + sign * offset;
        int cz = currentLocation.getBlockZ() + sign * offset;
        int y = sharedBaseY;

        Material concrete = (teamIndex == 0) ? Material.RED_CONCRETE : Material.BLUE_CONCRETE;
        int half = BASE_SIZE / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                int x = cx + dx, z = cz + dz;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);

                int localGroundY = findGroundY(world, x, z);
                // 플랫폼이 그 자리의 지면보다 떠 있으면 사이를 메워 공중에 뜬 것처럼 보이지 않게 함
                for (int fy = localGroundY + 1; fy < y; fy++) {
                    world.getBlockAt(x, fy, z).setType(concrete);
                }
                // 반대로 지면이 더 높으면 플랫폼 위로 흙이 덮이지 않도록 비워줌
                for (int fy = y + 1; fy <= localGroundY; fy++) {
                    world.getBlockAt(x, fy, z).setType(Material.AIR);
                }

                world.getBlockAt(x, y, z).setType(concrete);
            }
        }

        // 플랫폼이 주변 지면보다 높이 솟아 절벽처럼 막히면 올라올 수 없으므로,
        // 맵 중앙 방향으로 한 칸씩 내려가는 진입 계단을 만들어줌
        int rampDir = -sign;
        for (int step = 1; step <= MAX_RAMP_STEPS; step++) {
            int sx = cx + rampDir * (half + step);
            int sy = y - step;
            boolean reachedGround = true;
            for (int dz = -half; dz <= half; dz++) {
                int z = cz + dz;
                if (!world.isChunkLoaded(sx >> 4, z >> 4)) world.loadChunk(sx >> 4, z >> 4);

                int localGroundY = findGroundY(world, sx, z);
                if (sy <= localGroundY) continue; // 자연 지형이 이미 발판 역할을 함

                reachedGround = false;
                world.getBlockAt(sx, sy, z).setType(concrete);
                world.getBlockAt(sx, sy + 1, z).setType(Material.AIR);
                world.getBlockAt(sx, sy + 2, z).setType(Material.AIR);
            }
            if (reachedGround) break;
        }

        Location spawn = new Location(world, cx + 0.5, y + 1, cz + 0.5);
        int beaconY = spawn.getBlockY() + BEACON_HEIGHT;
        world.getBlockAt(cx, beaconY, cz).setType(Material.BEACON);

        Location guardianLoc = new Location(world, cx + 0.5, beaconY - 0.25, cz + 0.5);
        spawnBaseGuardian(world, guardianLoc, teamIndex);

        return spawn;
    }

    // 신호기 위치에 투명 슬라임 히트박스를 소환하고 체력을 보스바로 표시
    private void spawnBaseGuardian(World world, Location loc, int teamIndex) {
        String teamLabel = (teamIndex == 0) ? "레드팀" : "블루팀";
        NamedTextColor nameColor = (teamIndex == 0) ? NamedTextColor.RED : NamedTextColor.BLUE;
        BossBar.Color barColor = (teamIndex == 0) ? BossBar.Color.RED : BossBar.Color.BLUE;

        double[] lifeHealth = NexusSettings.lifeHealth();
        double initialHealth = lifeHealth[0];
        Slime guardian = world.spawn(loc, Slime.class, s -> {
            s.setSize(GUARDIAN_SLIME_SIZE);
            s.setAI(false);
            s.setInvisible(true);
            s.setSilent(true);
            s.setGravity(false);
            s.setPersistent(true);
            s.setRemoveWhenFarAway(false);
            s.setInvulnerable(false);
            var attr = s.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) attr.setBaseValue(initialHealth);
            s.setHealth(initialHealth);
        });

        BossBar bar = BossBar.bossBar(
            guardianBarTitle(teamLabel, initialHealth, initialHealth, nameColor),
            1.0f, barColor, BossBar.Overlay.NOTCHED_10);

        GuardianState state = new GuardianState(bar, teamLabel, nameColor, barColor, teamIndex, lifeHealth.length);
        guardianStates.put(guardian.getUniqueId(), state);
        if (Asurajang.getInstance().getGameManager().isGuardianAttackEnabled()) {
            state.attackTask = startGuardianAttackLoop(guardian, state);
        }
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);
    }

    // 거점 라이프 단계에 맞는 주기로 어그로 범위 내 상대팀을 탐지해 투사체를 발사하는 루프
    private BukkitTask startGuardianAttackLoop(LivingEntity guardian, GuardianState state) {
        long tickPeriod = NexusSettings.attackTickPeriod();
        state.attackCooldownTicks = currentAttackInterval(state);
        return Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), task -> {
            if (!guardian.isValid() || !guardianStates.containsKey(guardian.getUniqueId())) {
                task.cancel();
                return;
            }
            if (state.recovering) return;

            state.attackCooldownTicks -= NexusSettings.attackTickPeriod();
            if (state.attackCooldownTicks > 0) return;

            Player target = findNearestEnemy(guardian, state.teamIndex);
            if (target == null) {
                state.attackCooldownTicks = NexusSettings.attackTickPeriod();
                return;
            }

            fireGuardianProjectile(guardian, target, state);
            state.attackCooldownTicks = currentAttackInterval(state);
        }, 20L, tickPeriod);
    }

    // 현재 라이프 단계의 공격 주기를 반환 (체력이 늘어난 단계일수록 더 빠르게 공격)
    private static long currentAttackInterval(GuardianState state) {
        double[] lifeHealth = NexusSettings.lifeHealth();
        long[] intervals = NexusSettings.attackIntervalTicks();
        int stage = Math.max(0, Math.min(lifeHealth.length - state.lives, intervals.length - 1));
        return intervals[stage];
    }

    // 어그로 범위 안에서 가장 가까운 상대팀 플레이어를 찾는다 (관전·크리에이티브·아군·미배정 제외)
    @Nullable
    private Player findNearestEnemy(LivingEntity guardian, int defenderTeam) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        Player nearest = null;
        double aggroRange = NexusSettings.aggroRange();
        double nearestDistSq = aggroRange * aggroRange;

        for (Player p : guardian.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
            int team = gm.getTeam(p.getUniqueId());
            if (team == defenderTeam || team == -1) continue;

            double distSq = p.getLocation().distanceSquared(guardian.getLocation());
            if (distSq <= nearestDistSq) {
                nearest = p;
                nearestDistSq = distSq;
            }
        }
        return nearest;
    }

    // 팀 색상에 맞춘 더스트 파티클 옵션 (레드팀은 붉은빛, 블루팀은 푸른빛)
    private static Particle.DustOptions teamDustOptions(int teamIndex, float size) {
        return new Particle.DustOptions(
            teamIndex == 0 ? Color.fromRGB(255, 70, 70) : Color.fromRGB(80, 130, 255), size);
    }

    // 표적을 향해 유도되는 빛의 탄환을 발사 (틱마다 위치를 갱신하는 파티클 기반 커스텀 투사체)
    private void fireGuardianProjectile(LivingEntity guardian, Player target, GuardianState state) {
        World world = guardian.getWorld();
        Location current = guardian.getLocation().add(0, 0.5, 0);
        Particle.DustOptions dust = teamDustOptions(state.teamIndex, 1.2f);

        world.playSound(current, Sound.ENTITY_BLAZE_SHOOT, 1.0f, state.teamIndex == 0 ? 0.7f : 1.3f);

        int defenderTeam = state.teamIndex;
        int[] elapsed = { 0 };
        int maxLifeTicks = NexusSettings.projectileMaxLifeTicks();
        double hitRadius = NexusSettings.projectileHitRadius();
        double speed = NexusSettings.projectileSpeed();

        Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), projTask -> {
            if (!guardian.isValid() || !guardianStates.containsKey(guardian.getUniqueId())
                || elapsed[0]++ > maxLifeTicks) {
                projTask.cancel();
                return;
            }
            if (!target.isOnline() || target.isDead() || target.getGameMode() == GameMode.SPECTATOR) {
                projTask.cancel();
                return;
            }

            Vector toTarget = target.getEyeLocation().toVector().subtract(current.toVector());
            double distance = toTarget.length();
            if (distance <= hitRadius) {
                applyGuardianProjectileHit(guardian, target, defenderTeam);
                world.spawnParticle(Particle.DUST, current, 14, 0.2, 0.2, 0.2, 0.0, dust);
                world.spawnParticle(Particle.CRIT, current, 6, 0.15, 0.15, 0.15, 0.05);
                world.playSound(current, Sound.ENTITY_SHULKER_BULLET_HIT, 1.0f, 1.0f);
                projTask.cancel();
                return;
            }

            current.add(toTarget.normalize().multiply(speed));

            if (current.getBlock().getType().isSolid()) {
                world.spawnParticle(Particle.CRIT, current, 6, 0.1, 0.1, 0.1, 0.05);
                projTask.cancel();
                return;
            }

            world.spawnParticle(Particle.DUST, current, 3, 0.05, 0.05, 0.05, 0.0, dust);
        }, 0L, 1L);
    }

    // 명중 시 피해 적용 (혹시 모를 팀 오인 명중을 한 번 더 차단하는 안전장치)
    private void applyGuardianProjectileHit(LivingEntity guardian, Player target, int defenderTeam) {
        if (target.getGameMode() == GameMode.SPECTATOR) return;
        if (Asurajang.getInstance().getGameManager().getTeam(target.getUniqueId()) == defenderTeam) return;
        target.damage(NexusSettings.projectileDamage(), guardian);
    }

    private static Component guardianBarTitle(String teamLabel, double health, double maxHealth, NamedTextColor color) {
        return Component.text(teamLabel + " 거점", color)
            .append(Component.text("  " + Math.max(0, (int) Math.ceil(health)) + " / " + (int) maxHealth + " ❤", NamedTextColor.GRAY));
    }

    private static Component guardianRecoveringTitle(String teamLabel, int livesLeft, int secondsLeft, NamedTextColor color) {
        return Component.text(teamLabel + " 거점", color)
            .append(Component.text("  공격 불가 (" + secondsLeft + "초) · 잔여 라이프 " + livesLeft, NamedTextColor.GRAY));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGuardianDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity guardian)) return;
        GuardianState state = guardianStates.get(guardian.getUniqueId());
        if (state == null) return;

        // 같은 팀 플레이어가 입힌 피해는 무시 (아군 거점 보호)
        Player attacker = resolveGuardianAttacker(event);
        if (attacker != null && Asurajang.getInstance().getGameManager().getTeam(attacker.getUniqueId()) == state.teamIndex) {
            event.setCancelled(true);
            return;
        }

        // 신호기 블록 안에 끼어 압사 피해를 받지 않도록 무시
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true);
            return;
        }

        // 회복 대기 중에는 어떤 피해도 받지 않음
        if (state.recovering) {
            event.setCancelled(true);
            return;
        }

        // 실제로 피해가 들어갈 때마다 시련의 방 탐지 파티클로 "피격" 연출
        Location hitLoc = guardian.getLocation().add(0, 0.5, 0);
        guardian.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, hitLoc, 6, 0.5, 0.6, 0.5, 0);

        // 이번 피해로 체력이 모두 소진되면 실제로 죽이지 않고 라이프 차감 절차로 전환
        if (event.getFinalDamage() >= guardian.getHealth()) {
            event.setCancelled(true);
            depleteGuardianLife(guardian, state);
            return;
        }

        Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> updateGuardianBar(guardian, state));
    }

    // 직접 공격이든 투사체든 실제로 피해를 입힌 플레이어를 찾아낸다
    private static @Nullable Player resolveGuardianAttacker(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntity)) return null;
        Entity damager = byEntity.getDamager();
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onGuardianDeath(EntityDeathEvent event) {
        GuardianState state = guardianStates.remove(event.getEntity().getUniqueId());
        if (state == null) return;
        if (state.attackTask != null) state.attackTask.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(state.bar);
    }

    private void updateGuardianBar(LivingEntity guardian, GuardianState state) {
        if (!guardian.isValid() || state.recovering) return;
        var attr = guardian.getAttribute(Attribute.MAX_HEALTH);
        double[] lifeHealth = NexusSettings.lifeHealth();
        double max = (attr != null) ? attr.getValue() : lifeHealth[lifeHealth.length - state.lives];
        double cur = Math.max(0, guardian.getHealth());
        state.bar.progress((float) Math.max(0.0, Math.min(1.0, cur / max)));
        state.bar.name(guardianBarTitle(state.teamLabel, cur, max, state.color));
    }

    // 체력이 모두 소진됐을 때: 라이프를 차감하고, 남았으면 20초 공격 불가 후 전체 회복,
    // 모두 소진했으면 거점을 완전히 파괴
    private void depleteGuardianLife(LivingEntity guardian, GuardianState state) {
        state.lives--;

        if (state.lives <= 0) {
            Bukkit.broadcast(Component.text("[아수라장] ", NamedTextColor.GOLD)
                .append(Component.text(state.teamLabel + " 거점이 파괴되었습니다!", state.color)));
            if (state.attackTask != null) state.attackTask.cancel();
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(state.bar);
            guardianStates.remove(guardian.getUniqueId());
            guardian.remove();
            return;
        }

        state.recovering = true;
        state.bar.progress(0f);
        // 무적(회복) 중에는 보스바를 흰색으로 바꿔 체력이 서서히 차오르는 연출을 보여줌
        state.bar.color(BossBar.Color.WHITE);

        // 다음 라이프의 체력은 nexus.guardian.life-health 순서대로 점점 늘어남 (200 -> 300 -> 400)
        double[] lifeHealth = NexusSettings.lifeHealth();
        double nextMaxHealth = lifeHealth[lifeHealth.length - state.lives];

        Asurajang plugin = Asurajang.getInstance();

        // 공격 불가(무적) 상태인 동안 거점 주변에 불길한 징조(RAID_OMEN) · 시련의 징조(TRIAL_OMEN)와
        // 팀 색상 빛가루를 함께 표시해 "다음 라이프를 예고하는 재정비 중" 분위기를 연출
        Particle.DustOptions recoveryDust = teamDustOptions(state.teamIndex, 1.0f);
        Bukkit.getScheduler().runTaskTimer(plugin, smokeTask -> {
            if (!guardian.isValid() || !state.recovering) {
                smokeTask.cancel();
                return;
            }
            Location particleLoc = guardian.getLocation().add(0, 0.5, 0);
            World world = guardian.getWorld();
            world.spawnParticle(Particle.RAID_OMEN, particleLoc, 2, 0.45, 0.5, 0.45, 0);
            world.spawnParticle(Particle.TRIAL_OMEN, particleLoc, 2, 0.45, 0.5, 0.45, 0);
            world.spawnParticle(Particle.DUST, particleLoc, 4, 0.4, 0.5, 0.4, 0.0, recoveryDust);
        }, 0L, 4L);

        int total = NexusSettings.recoverySeconds();
        int[] remaining = { total };
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!guardian.isValid() || !guardianStates.containsKey(guardian.getUniqueId())) {
                task.cancel();
                return;
            }

            if (remaining[0] <= 0) {
                task.cancel();
                var attr = guardian.getAttribute(Attribute.MAX_HEALTH);
                if (attr != null) attr.setBaseValue(nextMaxHealth);
                guardian.setHealth(nextMaxHealth);
                state.recovering = false;
                state.bar.color(state.barColor);
                state.bar.progress(1f);
                state.bar.name(guardianBarTitle(state.teamLabel, nextMaxHealth, nextMaxHealth, state.color));
                return;
            }

            float fillProgress = (float) (total - remaining[0]) / total;
            state.bar.progress(Math.max(0f, Math.min(1f, fillProgress)));
            state.bar.name(guardianRecoveringTitle(state.teamLabel, state.lives, remaining[0], state.color));
            remaining[0]--;
        }, 0L, 20L);
    }

    // 게임 시작/종료 시 잔여 거점 히트박스와 보스바를 정리
    private void clearBaseGuardians() {
        for (Map.Entry<UUID, GuardianState> entry : guardianStates.entrySet()) {
            GuardianState state = entry.getValue();
            if (state.attackTask != null) state.attackTask.cancel();
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(state.bar);
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null) entity.remove();
        }
        guardianStates.clear();
    }

    // ── 조회 ────────────────────────────────────────────────────────────────

    @Nullable
    public Location getCurrentLocation() { return currentLocation; }

    public String getCurrentBiomeName() { return currentBiomeName; }

    // ── 내부 ────────────────────────────────────────────────────────────────

    private Biome pickNext() {
        List<Biome> available = new ArrayList<>(BIOME_POOL);
        available.removeAll(usedBiomes);

        if (available.isEmpty()) {
            usedBiomes.clear();
            available = new ArrayList<>(BIOME_POOL);
        }

        Biome picked = available.get(ThreadLocalRandom.current().nextInt(available.size()));
        usedBiomes.addLast(picked);
        if (usedBiomes.size() > HISTORY_SIZE) usedBiomes.removeFirst();
        return picked;
    }
}
