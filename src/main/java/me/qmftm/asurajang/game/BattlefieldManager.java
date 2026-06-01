package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BattlefieldManager {

    private static final int BORDER_RADIUS = 50;
    private static final int HISTORY_SIZE  = 3;          // 최근 N회 기록만 유지
    private static final int[] SEARCH_RADII = {3000, 6000, 12000, 20000}; // 단계별 탐색 반경

    private static final List<Biome> BIOME_POOL = List.of(
        Biome.PLAINS, Biome.SUNFLOWER_PLAINS,
        Biome.FOREST, Biome.FLOWER_FOREST, Biome.BIRCH_FOREST, Biome.DARK_FOREST,
        Biome.SAVANNA, Biome.SAVANNA_PLATEAU,
        Biome.DESERT, Biome.BADLANDS, Biome.WOODED_BADLANDS,
        Biome.TAIGA, Biome.SNOWY_PLAINS, Biome.SNOWY_TAIGA,
        Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.BAMBOO_JUNGLE,
        Biome.SWAMP, Biome.MANGROVE_SWAMP,
        Biome.MEADOW, Biome.CHERRY_GROVE
    );

    // 슬라이딩 윈도우: 최근 HISTORY_SIZE개만 제외
    private final LinkedList<Biome> usedBiomes = new LinkedList<>();

    private volatile Location currentLocation;
    private volatile String currentBiomeName = "";

    // ── 탐색 ────────────────────────────────────────────────────────────────

    public void searchAsync(World world, Runnable onComplete) {
        currentLocation = null;
        currentBiomeName = "";

        Biome target = pickNext();

        Bukkit.getScheduler().runTaskAsynchronously(Asurajang.getInstance(), () -> {
            Location origin = world.getSpawnLocation();
            Location found = null;

            // 반경을 단계적으로 늘려가며 탐색
            for (int radius : SEARCH_RADII) {
                found = world.locateNearestBiome(origin, target, radius);
                if (found != null) break;
            }

            final Location result = found;

            Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> {
                if (result != null) {
                    int chunkX = result.getBlockX() >> 4;
                    int chunkZ = result.getBlockZ() >> 4;
                    if (!world.isChunkLoaded(chunkX, chunkZ)) {
                        world.loadChunk(chunkX, chunkZ);
                    }
                    int y = world.getHighestBlockYAt(result.getBlockX(), result.getBlockZ());
                    currentLocation = new Location(world,
                        result.getBlockX() + 0.5, y + 1, result.getBlockZ() + 0.5);
                }
                currentBiomeName = formatBiome(target);
                onComplete.run();
            });
        });
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

        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            world.loadChunk(x >> 4, z >> 4);
        }

        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    // teamIndex 0 → NW 모서리, 1 → SE 모서리 (±5블록 랜덤 분산)
    @Nullable
    public Location getTeamCornerSpawn(int teamIndex) {
        if (currentLocation == null) return null;
        World world = currentLocation.getWorld();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int offset = BORDER_RADIUS - 10;
        int sign = (teamIndex == 0) ? -1 : 1;

        int x = currentLocation.getBlockX() + sign * offset + rng.nextInt(-5, 6);
        int z = currentLocation.getBlockZ() + sign * offset + rng.nextInt(-5, 6);

        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            world.loadChunk(x >> 4, z >> 4);
        }

        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    // ── 조회 ────────────────────────────────────────────────────────────────

    @Nullable
    public Location getCurrentLocation() {
        return currentLocation;
    }

    public String getCurrentBiomeName() {
        return currentBiomeName;
    }

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
        if (usedBiomes.size() > HISTORY_SIZE) {
            usedBiomes.removeFirst(); // 가장 오래된 기록 제거
        }

        return picked;
    }

    private static String formatBiome(Biome biome) {
        StringBuilder sb = new StringBuilder();
        for (String word : biome.name().toLowerCase().split("_")) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
