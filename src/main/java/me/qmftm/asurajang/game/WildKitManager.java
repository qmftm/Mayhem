package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;

public class WildKitManager {

    private static final String FILE_NAME = "wild_kit.yml";

    public static void save(Player player) throws IOException {
        File file = new File(Asurajang.getInstance().getDataFolder(), FILE_NAME);
        YamlConfiguration cfg = new YamlConfiguration();

        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) cfg.set("slots." + i, contents[i]);
        }
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) cfg.set("armor." + i, armor[i]);
        }

        cfg.save(file);
    }

    // 저장된 야생 킷을 플레이어에게 적용. 킷 파일이 없으면 false 반환.
    public static boolean applyIfExists(Player player) {
        File file = new File(Asurajang.getInstance().getDataFolder(), FILE_NAME);
        if (!file.exists()) return false;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        PlayerInventory inv = player.getInventory();
        if (cfg.contains("slots")) {
            for (String key : cfg.getConfigurationSection("slots").getKeys(false)) {
                int slot = Integer.parseInt(key);
                inv.setItem(slot, cfg.getItemStack("slots." + key));
            }
        }
        if (cfg.contains("armor")) {
            ItemStack[] armor = new ItemStack[4];
            for (String key : cfg.getConfigurationSection("armor").getKeys(false)) {
                int i = Integer.parseInt(key);
                if (i < 4) armor[i] = cfg.getItemStack("armor." + key);
            }
            inv.setArmorContents(armor);
        }

        return true;
    }

    public static boolean exists() {
        return new File(Asurajang.getInstance().getDataFolder(), FILE_NAME).exists();
    }
}
