package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BlackFlashEffect implements AugmentationEffect {

    public static boolean debugProc = false;
    public static final Set<UUID> pendingKnockback    = ConcurrentHashMap.newKeySet();
    // target UUID → attacker UUID: 흑섬 발동 후 사망 시 파티클 표시용
    public static final Map<UUID, UUID> pendingDeathParticle = new ConcurrentHashMap<>();

    private static final Particle.DustOptions BLACK_DUST        = new Particle.DustOptions(Color.BLACK, 1.1f);
    private static final Particle.DustOptions RED_DUST          = new Particle.DustOptions(Color.RED,   0.7f);
    private static final Particle.DustOptions BRANCH_BLACK_DUST = new Particle.DustOptions(Color.BLACK, 1.65f);
    private static final Particle.DustOptions BRANCH_RED_DUST   = new Particle.DustOptions(Color.RED,   1.05f);

    // 발동 후 남은 연속 공격 횟수 (3→50%, 2→35%, 1→20%, 0→5%)
    private int streakRemaining = 0;

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        streakRemaining = 0;
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        java.util.List<Double> streakChances = AugmentSettings.getDoubleList("BlackFlash", "streak-chances", java.util.List.of(0.36, 0.18, 0.06));
        double baseChance = AugmentSettings.getDouble("BlackFlash", "base-chance", 0.03);
        int streakReset = AugmentSettings.getInt("BlackFlash", "streak-reset", 3);

        double chance = baseChance;
        if (streakRemaining > 0) {
            int idx = streakReset - streakRemaining;
            if (idx >= 0 && idx < streakChances.size()) chance = streakChances.get(idx);
            streakRemaining--;
        }

        LivingEntity target = (LivingEntity) event.getEntity();

        if (debugProc || ThreadLocalRandom.current().nextDouble() < chance) {
            streakRemaining = streakReset;
            event.setDamage(event.getDamage() * AugmentSettings.getDouble("BlackFlash", "damage-multiplier", 2.5));

            World world = target.getWorld();

            // 넉백 4배: EntityKnockbackByEntityEvent에서 처리
            pendingKnockback.add(player.getUniqueId());

            // 사망 파티클 대상 등록
            pendingDeathParticle.put(target.getUniqueId(), player.getUniqueId());
            Location loc = target.getLocation().add(0, 1, 0);

            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.6f);
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT,  1.0f, 1.9f);
            player.sendActionBar(Component.text("흑섬", Style.style()
                .color(NamedTextColor.BLACK)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .shadowColor(ShadowColor.shadowColor(0xFFFF0000))
                .build()));

            // 0.1초(2틱) 간격으로 10회 = 1초 지속
            int[] count = {0};
            Asurajang plugin = Asurajang.getInstance();

            // DUST_COLOR_TRANSITION: 반경 5칸, 5~8개, 0.25초(5틱)마다 4번
            int[] transCount = {0};
            plugin.getServer().getScheduler().runTaskTimer(plugin, transTask -> {
                if (transCount[0] >= 4 || !target.isValid() || target.isDead()) {
                    transTask.cancel();
                    return;
                }
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                Location base = target.getLocation().add(0, 1, 0);
                Particle.DustOptions bigRedDust = new Particle.DustOptions(Color.RED, 3.3f);
                int amount = 5 + rng.nextInt(4); // 5~8개
                for (int i = 0; i < amount; i++) {
                    double offX = (rng.nextDouble() - 0.5) * 6.0; // -3 ~ +3
                    double offZ = (rng.nextDouble() - 0.5) * 6.0; // -3 ~ +3
                    double offY = rng.nextDouble() * 2.0;
                    Location pos  = base.clone().add(offX, offY, offZ);
                    Particle.DustTransition trans = rng.nextBoolean()
                        ? new Particle.DustTransition(Color.BLACK, Color.RED,   8.25f)
                        : new Particle.DustTransition(Color.RED,   Color.BLACK, 8.25f);
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos, 1, 0, 0, 0, 0.0, trans);
                    world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0.0, bigRedDust);
                }
                transCount[0]++;
            }, 0L, 5L);

            plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                if (count[0] >= 10 || !target.isValid() || target.isDead()) {
                    task.cancel();
                    return;
                }
                spawnBlackLightning(target.getLocation().add(0, 1, 0), world);
                count[0]++;
            }, 0L, 2L);
        } else {
            // proc 실패 시 이 공격자가 등록한 사망 파티클만 제거
            pendingDeathParticle.remove(target.getUniqueId(), player.getUniqueId());
        }
    }

    private void spawnBlackLightning(Location center, World world) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // 중심 폭발
        world.spawnParticle(Particle.DUST, center, 25, 0.08, 0.12, 0.08, 0.0, BLACK_DUST);
        world.spawnParticle(Particle.DUST, center, 28, 0.10, 0.14, 0.10, 0.0, RED_DUST);

        // 메인 갈래 (1~3개)
        int branches = 1 + rng.nextInt(3);
        for (int i = 0; i < branches; i++) {
            double theta = rng.nextDouble() * Math.PI * 2;
            double phi   = (rng.nextDouble() - 0.5) * Math.PI * 0.65;

            Vector dir = new Vector(
                Math.cos(theta) * Math.cos(phi),
                Math.sin(phi),
                Math.sin(theta) * Math.cos(phi)
            ).normalize();

            double length = 1.8 + rng.nextDouble() * 2.5;

            for (double d = 0.15; d < length; d += 0.15) {
                Location pos = center.clone().add(
                    dir.clone().multiply(d).add(new Vector(
                        (rng.nextDouble() - 0.5) * 0.10,
                        (rng.nextDouble() - 0.5) * 0.10,
                        (rng.nextDouble() - 0.5) * 0.10
                    ))
                );
                world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BRANCH_BLACK_DUST);
                if (rng.nextDouble() < 0.65) {
                    world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BRANCH_RED_DUST);
                }
            }

            // 서브 갈래 2~3개 (55% 확률)
            if (length > 2.0 && rng.nextDouble() < 0.55) {
                int subCount = 1 + rng.nextInt(2); // 1~2개
                for (int s = 0; s < subCount; s++) {
                    double subDist = length * 0.25 + rng.nextDouble() * length * 0.4;
                    Location subOrigin = center.clone().add(dir.clone().multiply(subDist));

                    Vector subDir = new Vector(
                        dir.getX() + (rng.nextDouble() - 0.5) * 1.4,
                        dir.getY() + (rng.nextDouble() - 0.5) * 0.6,
                        dir.getZ() + (rng.nextDouble() - 0.5) * 1.4
                    ).normalize();

                    double subLen = 0.4 + rng.nextDouble() * 1.0;
                    for (double d = 0.15; d < subLen; d += 0.15) {
                        Location pos = subOrigin.clone().add(subDir.clone().multiply(d));
                        world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BRANCH_BLACK_DUST);
                        if (rng.nextDouble() < 0.55) {
                            world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BRANCH_RED_DUST);
                        }
                    }
                }
            }
        }
    }
}
