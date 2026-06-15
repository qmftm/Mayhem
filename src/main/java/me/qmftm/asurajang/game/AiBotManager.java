package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// 디버그용 AI 봇 (팀 색상의 마네킹) 관리. 마네킹은 자체 AI가 없으므로
// 타겟 탐색, 이동, 근접 공격을 직접 구현한다.
public class AiBotManager {

    private static final double TARGET_RANGE  = 40.0;
    private static final double ATTACK_RANGE  = 2.2;
    private static final double MOVE_STEP     = 0.25;
    private static final double BASE_DAMAGE   = 4.0;
    private static final long ATTACK_COOLDOWN_TICKS = 20L;
    private static final long AI_PERIOD_TICKS = 2L;

    private final Map<UUID, Integer> botTeams      = new HashMap<>();
    private final Map<UUID, Long>    nextAttackTick = new HashMap<>();
    private BukkitTask aiTask;
    private long tickCounter = 0;

    public void spawnBot(Location location, int team) {
        Color armorColor = team == 0 ? Color.fromRGB(255, 70, 70) : Color.fromRGB(80, 130, 255);
        Component name = Component.text("AI (" + (team == 0 ? "레드팀" : "블루팀") + ")",
            team == 0 ? NamedTextColor.RED : NamedTextColor.BLUE);

        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class, m -> {
            m.setProfile(ResolvableProfile.resolvableProfile(
                Bukkit.createProfile(UUID.randomUUID(), team == 0 ? "AI_Red" : "AI_Blue")));
            m.customName(name);
            m.setCustomNameVisible(true);
            m.setImmovable(false);

            EntityEquipment eq = m.getEquipment();
            if (eq != null) {
                eq.setHelmet(dyedLeather(Material.LEATHER_HELMET, armorColor));
                eq.setChestplate(dyedLeather(Material.LEATHER_CHESTPLATE, armorColor));
                eq.setLeggings(dyedLeather(Material.LEATHER_LEGGINGS, armorColor));
                eq.setBoots(dyedLeather(Material.LEATHER_BOOTS, armorColor));
                eq.setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
            }
        });

        botTeams.put(mannequin.getUniqueId(), team);
        Asurajang.getInstance().getScoreboardManager().addEntityToTeam(mannequin, team);
        ensureAiTask();
    }

    private ItemStack dyedLeather(Material type, Color color) {
        ItemStack item = new ItemStack(type);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    private void ensureAiTask() {
        if (aiTask != null) return;
        aiTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), this::tick, AI_PERIOD_TICKS, AI_PERIOD_TICKS);
    }

    private void tick() {
        tickCounter += AI_PERIOD_TICKS;
        GameManager gm = Asurajang.getInstance().getGameManager();

        for (Map.Entry<UUID, Integer> entry : botTeams.entrySet()) {
            Entity entity = Asurajang.getInstance().getServer().getEntity(entry.getKey());
            if (!(entity instanceof Mannequin mannequin) || !mannequin.isValid()) continue;

            Player target = findNearestEnemy(mannequin, entry.getValue(), gm);
            if (target == null) {
                Location beacon = Asurajang.getInstance().getBattlefieldManager().getTeamBeaconLocation(1 - entry.getValue());
                if (beacon != null) moveToward(mannequin, beacon);
                continue;
            }

            if (target.getLocation().distanceSquared(mannequin.getLocation()) > ATTACK_RANGE * ATTACK_RANGE) {
                moveToward(mannequin, target.getLocation());
            } else {
                faceTarget(mannequin, target.getLocation());
                tryAttack(mannequin, target);
            }
        }
    }

    private Player findNearestEnemy(Mannequin mannequin, int botTeam, GameManager gm) {
        Player nearest = null;
        double nearestDist = TARGET_RANGE * TARGET_RANGE;

        for (Player p : mannequin.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            if (gm.getTeam(p.getUniqueId()) == botTeam) continue;

            double dist = p.getLocation().distanceSquared(mannequin.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    private void moveToward(Mannequin mannequin, Location targetLoc) {
        Location current = mannequin.getLocation();
        Vector dir = targetLoc.toVector().subtract(current.toVector());
        dir.setY(0);
        if (dir.lengthSquared() < 1.0E-4) return;
        dir.normalize().multiply(MOVE_STEP);

        World world = current.getWorld();
        double newX = current.getX() + dir.getX();
        double newZ = current.getZ() + dir.getZ();
        int blockX = (int) Math.floor(newX);
        int blockZ = (int) Math.floor(newZ);
        if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) world.loadChunk(blockX >> 4, blockZ >> 4);
        double newY = world.getHighestBlockYAt(blockX, blockZ) + 1.0;

        Location next = new Location(world, newX, newY, newZ);
        faceLocation(next, targetLoc);
        mannequin.teleport(next);
    }

    private void faceTarget(Mannequin mannequin, Location targetLoc) {
        Location current = mannequin.getLocation().clone();
        faceLocation(current, targetLoc);
        mannequin.teleport(current);
    }

    private void faceLocation(Location from, Location target) {
        Vector dir = target.toVector().subtract(from.toVector());
        if (dir.lengthSquared() < 1.0E-4) return;
        from.setYaw((float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ())));
        from.setPitch(0f);
    }

    // 기본 공격에 육중한 힘(HeavyForce) 피해량 배율을 적용하고,
    // 흑섬(BlackFlash) 확률로 치명타 + 강한 넉백을 발동시킨다
    private void tryAttack(Mannequin mannequin, Player target) {
        long ready = nextAttackTick.getOrDefault(mannequin.getUniqueId(), 0L);
        if (tickCounter < ready) return;
        nextAttackTick.put(mannequin.getUniqueId(), tickCounter + ATTACK_COOLDOWN_TICKS);

        double damage = BASE_DAMAGE * AugmentSettings.getDouble("HeavyForce", "damage-multiplier", 1.2);
        Vector knockback = target.getLocation().toVector()
            .subtract(mannequin.getLocation().toVector())
            .setY(0.1)
            .normalize();

        if (ThreadLocalRandom.current().nextDouble() < AugmentSettings.getDouble("BlackFlash", "base-chance", 0.03)) {
            damage *= AugmentSettings.getDouble("BlackFlash", "damage-multiplier", 2.5);
            knockback.multiply(AugmentSettings.getDouble("BlackFlash", "knockback-multiplier", 4.0));

            Location loc = target.getLocation().add(0, 1, 0);
            target.getWorld().spawnParticle(Particle.DUST, loc, 25, 0.3, 0.3, 0.3, 0.1,
                new Particle.DustOptions(Color.fromRGB(20, 20, 20), 1.2f));
            target.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.6f);
        } else {
            knockback.multiply(0.4);
        }

        mannequin.swingMainHand();
        target.damage(damage, mannequin);
        target.setVelocity(target.getVelocity().add(knockback));
    }

    // 봇 UUID -> 소속 팀(0/1) 매핑 (읽기 전용)
    public Map<UUID, Integer> getBotTeams() {
        return Collections.unmodifiableMap(botTeams);
    }

    public void clearAll() {
        if (aiTask != null) {
            aiTask.cancel();
            aiTask = null;
        }

        for (UUID uuid : botTeams.keySet()) {
            Entity entity = Asurajang.getInstance().getServer().getEntity(uuid);
            if (entity != null) {
                Asurajang.getInstance().getScoreboardManager().removeEntityFromTeams(entity);
                entity.remove();
            }
        }
        botTeams.clear();
        nextAttackTick.clear();
        tickCounter = 0;
    }
}
