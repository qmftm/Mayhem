package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class HeugsomEffect implements AugmentationEffect {

    public static boolean debugProc = false;

    private static final Particle.DustOptions BLACK_DUST = new Particle.DustOptions(Color.BLACK, 1.1f);
    private static final Particle.DustOptions RED_DUST   = new Particle.DustOptions(Color.RED,   0.7f);

    private int hitCount = 0;

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {
        hitCount = 0;
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        hitCount++;

        if (hitCount % 5 == 0 && (debugProc || ThreadLocalRandom.current().nextDouble() < 0.20)) {
            event.setDamage(event.getDamage() * 2.5);

            LivingEntity target = (LivingEntity) event.getEntity();
            World world = target.getWorld();
            Location loc = target.getLocation().add(0, 1, 0);

            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.6f);
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT,  1.0f, 1.9f);
            player.sendActionBar(Component.text("흑섬 발동!", NamedTextColor.DARK_GRAY));

            // 0.1초(2틱) 간격으로 10회 = 1초 지속
            int[] count = {0};
            Asurajang plugin = Asurajang.getInstance();
            plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                if (count[0] >= 10 || !target.isValid() || target.isDead()) {
                    task.cancel();
                    return;
                }
                spawnBlackLightning(target.getLocation().add(0, 1, 0), world);
                count[0]++;
            }, 0L, 2L);
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
                world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BLACK_DUST);
                if (rng.nextDouble() < 0.65) {
                    world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, RED_DUST);
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
                        world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, BLACK_DUST);
                        if (rng.nextDouble() < 0.55) {
                            world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0, RED_DUST);
                        }
                    }
                }
            }
        }
    }
}
