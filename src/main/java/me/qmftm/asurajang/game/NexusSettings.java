package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * nexus.yml의 guardian.<key> 경로에서 거점 가디언 관련 수치를 읽어오는 유틸리티.
 * 매번 파일에서 직접 읽어오므로 /mayhem reload로 즉시 반영된다.
 */
public final class NexusSettings {

    private NexusSettings() {
    }

    private static ConfigurationSection guardianSection() {
        return Asurajang.getInstance().getNexusConfig().getConfigurationSection("guardian");
    }

    public static double[] lifeHealth() {
        ConfigurationSection section = guardianSection();
        List<Double> list = (section == null || !section.isSet("life-health"))
            ? List.of(200.0, 300.0, 400.0)
            : section.getDoubleList("life-health");
        double[] result = new double[list.size()];
        for (int i = 0; i < result.length; i++) result[i] = list.get(i);
        return result;
    }

    public static int recoverySeconds() {
        return getInt("recovery-seconds", 20);
    }

    public static double aggroRange() {
        return getDouble("aggro-range", 16.0);
    }

    public static double projectileDamage() {
        return getDouble("projectile-damage", 5.0);
    }

    public static double projectileSpeed() {
        return getDouble("projectile-speed", 1.1);
    }

    public static double projectileHitRadius() {
        return getDouble("projectile-hit-radius", 1.0);
    }

    public static int projectileMaxLifeTicks() {
        return getInt("projectile-max-life-ticks", 100);
    }

    public static long attackTickPeriod() {
        return getLong("attack-tick-period", 5L);
    }

    public static long[] attackIntervalTicks() {
        ConfigurationSection section = guardianSection();
        List<Long> list = (section == null || !section.isSet("attack-interval-ticks"))
            ? List.of(65L, 45L, 28L)
            : section.getLongList("attack-interval-ticks");
        long[] result = new long[list.size()];
        for (int i = 0; i < result.length; i++) result[i] = list.get(i);
        return result;
    }

    private static double getDouble(String key, double def) {
        ConfigurationSection section = guardianSection();
        return section == null ? def : section.getDouble(key, def);
    }

    private static int getInt(String key, int def) {
        ConfigurationSection section = guardianSection();
        return section == null ? def : section.getInt(key, def);
    }

    private static long getLong(String key, long def) {
        ConfigurationSection section = guardianSection();
        return section == null ? def : section.getLong(key, def);
    }
}
