package me.qmftm.asurajang.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * config.yml 외에 플러그인 데이터 폴더에 별도로 두는 yml 리소스를 로드·재로드한다.
 * 파일이 없으면 jar에 포함된 기본 리소스를 복사해 생성한다.
 */
public final class YamlResource {

    private final JavaPlugin plugin;
    private final String resourcePath;
    private File file;
    private FileConfiguration config;

    public YamlResource(JavaPlugin plugin, String resourcePath) {
        this.plugin = plugin;
        this.resourcePath = resourcePath;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }
}
