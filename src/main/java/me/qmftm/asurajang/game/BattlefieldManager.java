package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private static final int LOCATE_RADIUS     = 25600; // /locate biome 기준 반경
    private static final int LOCATE_STEP_HORIZ = 32;    // /locate biome 기준 수평 간격
    private static final int LOCATE_STEP_VERT  = 64;    // /locate biome 기준 수직 간격

    private static final List<Biome> BIOME_POOL = List.of(
        Biome.PLAINS,          Biome.CHERRY_GROVE,
        Biome.DESERT,          Biome.SNOWY_PLAINS,
        Biome.JAGGED_PEAKS,    Biome.BADLANDS,
        Biome.MEADOW,          Biome.WINDSWEPT_HILLS,
        Biome.SUNFLOWER_PLAINS, Biome.FROZEN_RIVER
    );

    private static final Map<Biome, String> BIOME_NAMES = Map.of(
        Biome.PLAINS,           "평원",
        Biome.CHERRY_GROVE,     "벚꽃 숲",
        Biome.DESERT,           "사막",
        Biome.SNOWY_PLAINS,     "눈 덮인 평원",
        Biome.JAGGED_PEAKS,     "역고드름",
        Biome.BADLANDS,         "메사",
        Biome.MEADOW,           "목초지",
        Biome.WINDSWEPT_HILLS,  "바람이 세찬 언덕",
        Biome.SUNFLOWER_PLAINS, "해바라기 평원",
        Biome.FROZEN_RIVER,     "얼어붙은 강"
    );

    private static final Map<String, NamedTextColor> BIOME_COLORS = Map.of(
        "평원",             NamedTextColor.GREEN,
        "벚꽃 숲",          NamedTextColor.LIGHT_PURPLE,
        "사막",             NamedTextColor.GOLD,
        "눈 덮인 평원",     NamedTextColor.AQUA,
        "역고드름",         NamedTextColor.GRAY,
        "메사",             NamedTextColor.RED,
        "목초지",           NamedTextColor.DARK_GREEN,
        "바람이 세찬 언덕", NamedTextColor.DARK_GRAY,
        "해바라기 평원",    NamedTextColor.YELLOW,
        "얼어붙은 강",      NamedTextColor.DARK_AQUA
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

        // 매 게임마다 다른 위치의 바이옴을 찾기 위해 랜덤 좌표를 탐색 기점으로 사용
        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        double dist  = 2000 + ThreadLocalRandom.current().nextDouble() * 6000; // 2000~8000블록
        Location randomOrigin = new Location(world,
            Math.cos(angle) * dist, 64, Math.sin(angle) * dist);

        Bukkit.getScheduler().runTaskAsynchronously(Asurajang.getInstance(), () -> {
            var result = world.locateNearestBiome(
                randomOrigin, LOCATE_RADIUS, LOCATE_STEP_HORIZ, LOCATE_STEP_VERT, target);
            Location found = result != null ? result.getLocation() : null;

            Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> {
                if (found != null) {
                    int chunkX = found.getBlockX() >> 4;
                    int chunkZ = found.getBlockZ() >> 4;
                    if (!world.isChunkLoaded(chunkX, chunkZ)) world.loadChunk(chunkX, chunkZ);
                    int y = world.getHighestBlockYAt(found.getBlockX(), found.getBlockZ());
                    currentLocation = new Location(world,
                        found.getBlockX() + 0.5, y + 1, found.getBlockZ() + 0.5);
                }
                currentBiomeName = targetName;
                onComplete.run();
            });
        });
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
