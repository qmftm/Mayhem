package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

// 드래곤의 머리를 왼손에 들고 있는 동안 강력한 버프를 지속적으로 부여한다.
public class DragonEffect implements AugmentationEffect {

    private BukkitTask offHandTask;
    private boolean buffActive = false;

    @Override
    public void onActivate(Player player) {
        buffActive = false;
        giveDragonHead(player);

        long interval = AugmentSettings.getLong("Dragon", "offhand-check-interval-ticks", 5L);
        offHandTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> tickOffHand(player), 0L, interval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (offHandTask != null) { offHandTask.cancel(); offHandTask = null; }
        if (buffActive) clearBuffs(player);
        buffActive = false;
    }

    private void tickOffHand(Player player) {
        if (!player.isOnline()) return;

        boolean held = isDragonHead(player.getInventory().getItemInOffHand());
        if (held) {
            applyBuffs(player);
            buffActive = true;
        } else if (buffActive) {
            clearBuffs(player);
            buffActive = false;
        }
    }

    private void applyBuffs(Player player) {
        long interval = AugmentSettings.getLong("Dragon", "offhand-check-interval-ticks", 5L);
        int duration = (int) interval + 5;

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
            duration, AugmentSettings.getInt("Dragon", "strength-amplifier", 1), true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
            duration, AugmentSettings.getInt("Dragon", "resistance-amplifier", 1), true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
            duration, AugmentSettings.getInt("Dragon", "speed-amplifier", 0), true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
            duration, AugmentSettings.getInt("Dragon", "regeneration-amplifier", 1), true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST,
            duration, AugmentSettings.getInt("Dragon", "health-boost-amplifier", 3), true, false));
    }

    private void clearBuffs(Player player) {
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
    }

    private static void giveDragonHead(Player player) {
        ItemStack item = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("드래곤", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("왼손에 들고 있으면 강력한 버프를 받습니다.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING, "Dragon");
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

    private static boolean isDragonHead(ItemStack item) {
        if (item == null || item.getType() != Material.DRAGON_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "Dragon".equals(meta.getPersistentDataContainer().get(Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING));
    }
}
