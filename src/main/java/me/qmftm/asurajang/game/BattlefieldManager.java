package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BattlefieldManager {

    private static final int BORDER_RADIUS = 50;
    private static final int HISTORY_SIZE  = 3;
    private static final int LOCATE_RADIUS     = 25600;
    private static final int LOCATE_STEP_HORIZ = 32;
    private static final int LOCATE_STEP_VERT  = 64;

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

    private final LinkedList<Biome> usedBiomes = new LinkedList<>();

    private volatile Location currentLocation;
    private volatile String currentBiomeName = "";

    // ── 탐색 ────────────────────────────────────────────────────────────────

    public void searchAsync(World world, Runnable onComplete) {
        currentLocation = null;
        currentBiomeName = "";

        Biome target = pickNext();
        String targetName = BIOME_NAMES.getOrDefault(target, target.name());

        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        double dist  = 2000 + ThreadLocalRandom.current().nextDouble() * 6000;
        Location randomOrigin = new Location(world,
            Math.cos(angle) * dist, 64, Math.sin(angle) * dist);

        Bukkit.getScheduler().runTaskAsynchronously(Asurajang.getInstance(), () -> {
            var result = world.locateNearestBiome(
                randomOrigin, LOCATE_RADIUS, LOCATE_STEP_HORIZ, LOCATE_STEP_VERT, target);
            Location found = result != null ? result.getLocation() : null;

            Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> {
                if (found != null) {
                    Location dry = findDryLocation(world, found);
                    int chunkX = dry.getBlockX() >> 4;
                    int chunkZ = dry.getBlockZ() >> 4;
                    if (!world.isChunkLoaded(chunkX, chunkZ)) world.loadChunk(chunkX, chunkZ);
                    int y = world.getHighestBlockYAt(dry.getBlockX(), dry.getBlockZ());
                    currentLocation = new Location(world,
                        dry.getBlockX() + 0.5, y + 1, dry.getBlockZ() + 0.5);
                }
                currentBiomeName = targetName;
                onComplete.run();
            });
        });
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

    public void applyBorder() {
        if (currentLocation == null) return;
        WorldBorder border = currentLocation.getWorld().getWorldBorder();
        border.setCenter(currentLocation.getX(), currentLocation.getZ());
        border.setSize(BORDER_RADIUS * 2);
    }

    public void resetBorder() {
        Bukkit.getWorlds().get(0).getWorldBorder().reset();
    }

    // ── 랜덤 스폰 ────────────────────────────────────────────────────────────

    @Nullable
    public Location getRandomSpawn() {
        if (currentLocation == null) return null;
        World world = currentLocation.getWorld();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int margin = BORDER_RADIUS - 10;

        int x = currentLocation.getBlockX() + rng.nextInt(-margin, margin + 1);
        int z = currentLocation.getBlockZ() + rng.nextInt(-margin, margin + 1);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    @Nullable
    public Location getTeamCornerSpawn(int teamIndex) {
        if (currentLocation == null) return null;
        World world = currentLocation.getWorld();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int offset = BORDER_RADIUS - 10;
        int sign = (teamIndex == 0) ? -1 : 1;

        int x = currentLocation.getBlockX() + sign * offset + rng.nextInt(-5, 6);
        int z = currentLocation.getBlockZ() + sign * offset + rng.nextInt(-5, 6);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
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
