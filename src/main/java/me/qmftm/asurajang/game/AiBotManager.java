package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 디버그용 AI 봇 (팀 색상의 좀비형 허스크) 관리
public class AiBotManager {

    private static final double TARGET_RANGE = 40.0;

    private final Map<UUID, Integer> bots = new HashMap<>();
    private BukkitTask targetingTask;

    public void spawnBot(Location location, int team) {
        Color armorColor = team == 0 ? Color.fromRGB(255, 70, 70) : Color.fromRGB(80, 130, 255);
        Component name = Component.text("AI (" + (team == 0 ? "레드팀" : "블루팀") + ")",
            team == 0 ? NamedTextColor.RED : NamedTextColor.BLUE);

        Husk husk = location.getWorld().spawn(location, Husk.class, h -> {
            h.setShouldBurnInDay(false);
            h.setPersistent(true);
            h.setRemoveWhenFarAway(false);
            h.setCanPickupItems(false);
            h.customName(name);
            h.setCustomNameVisible(true);

            EntityEquipment eq = h.getEquipment();
            if (eq != null) {
                eq.setHelmet(dyedLeather(Material.LEATHER_HELMET, armorColor));
                eq.setChestplate(dyedLeather(Material.LEATHER_CHESTPLATE, armorColor));
                eq.setLeggings(dyedLeather(Material.LEATHER_LEGGINGS, armorColor));
                eq.setBoots(dyedLeather(Material.LEATHER_BOOTS, armorColor));
                eq.setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
            }
        });

        bots.put(husk.getUniqueId(), team);
        Asurajang.getInstance().getScoreboardManager().addEntityToTeam(husk, team);
        ensureTargetingTask();
    }

    private ItemStack dyedLeather(Material type, Color color) {
        ItemStack item = new ItemStack(type);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    private void ensureTargetingTask() {
        if (targetingTask != null) return;
        targetingTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), this::retarget, 20L, 20L);
    }

    // 매 틱마다 같은 팀이 아닌 가장 가까운 플레이어를 공격 대상으로 지정
    private void retarget() {
        GameManager gm = Asurajang.getInstance().getGameManager();

        for (Map.Entry<UUID, Integer> entry : bots.entrySet()) {
            Entity entity = Asurajang.getInstance().getServer().getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isValid()) continue;

            int botTeam = entry.getValue();
            Player nearest = null;
            double nearestDist = TARGET_RANGE * TARGET_RANGE;

            for (Player p : mob.getWorld().getPlayers()) {
                if (gm.getTeam(p.getUniqueId()) == botTeam) continue;
                double dist = p.getLocation().distanceSquared(mob.getLocation());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = p;
                }
            }

            if (nearest != null) mob.setTarget(nearest);
        }
    }

    public void clearAll() {
        if (targetingTask != null) {
            targetingTask.cancel();
            targetingTask = null;
        }

        for (UUID uuid : bots.keySet()) {
            Entity entity = Asurajang.getInstance().getServer().getEntity(uuid);
            if (entity != null) {
                Asurajang.getInstance().getScoreboardManager().removeEntityFromTeams(entity);
                entity.remove();
            }
        }
        bots.clear();
    }
}
