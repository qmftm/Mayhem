package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BlacklistManager {

    private final File file;
    private final Set<String> blacklisted = new HashSet<>();

    public BlacklistManager() {
        this.file = new File(Asurajang.getInstance().getDataFolder(), "blacklist.yml");
        load();
    }

    private void load() {
        blacklisted.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        blacklisted.addAll(config.getStringList("blacklist"));
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("blacklist", blacklisted.stream().sorted().toList());
        try {
            config.save(file);
        } catch (IOException e) {
            Asurajang.getInstance().getLogger().warning("블랙리스트 저장 실패: " + e.getMessage());
        }
    }

    public boolean isBlacklisted(String id) {
        return blacklisted.contains(id);
    }

    public boolean toggle(String id) {
        boolean added;
        if (blacklisted.contains(id)) {
            blacklisted.remove(id);
            added = false;
        } else {
            blacklisted.add(id);
            added = true;
        }
        save();
        return added;
    }

    public void reload() {
        load();
    }
}
