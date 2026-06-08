package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * augment/setting.yml의 <증강 ID>.<key> 경로에서
 * 확률·피해량 등의 수치를 읽어오는 유틸리티.
 * 매번 파일에서 직접 읽어오므로 /mayhem reload로 즉시 반영된다.
 */
public final class AugmentSettings {

    private AugmentSettings() {
    }

    private static ConfigurationSection section(String augmentationId) {
        return Asurajang.getInstance().getAugmentSettingConfig().getConfigurationSection(augmentationId);
    }

    public static double getDouble(String augmentationId, String key, double def) {
        ConfigurationSection section = section(augmentationId);
        return section == null ? def : section.getDouble(key, def);
    }

    public static int getInt(String augmentationId, String key, int def) {
        ConfigurationSection section = section(augmentationId);
        return section == null ? def : section.getInt(key, def);
    }

    public static long getLong(String augmentationId, String key, long def) {
        ConfigurationSection section = section(augmentationId);
        return section == null ? def : section.getLong(key, def);
    }

    public static List<Double> getDoubleList(String augmentationId, String key, List<Double> def) {
        ConfigurationSection section = section(augmentationId);
        if (section == null || !section.isSet(key)) return def;
        return section.getDoubleList(key);
    }

    public static List<Integer> getIntList(String augmentationId, String key, List<Integer> def) {
        ConfigurationSection section = section(augmentationId);
        if (section == null || !section.isSet(key)) return def;
        return section.getIntegerList(key);
    }

    public static List<Long> getLongList(String augmentationId, String key, List<Long> def) {
        ConfigurationSection section = section(augmentationId);
        if (section == null || !section.isSet(key)) return def;
        return section.getLongList(key);
    }
}
