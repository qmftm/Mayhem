package me.qmftm.asurajang.game;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaxHealthManager {

    private static final double DEFAULT_BASE = 20.0;

    private final Map<UUID, Double> base = new HashMap<>();

    // ── 라이프사이클 ─────────────────────────────────────────────────────────

    public void setup(UUID uuid) {
        base.put(uuid, DEFAULT_BASE);
    }

    public void remove(UUID uuid) {
        base.remove(uuid);
    }

    // ── 기본 최대 체력 ────────────────────────────────────────────────────────

    public double getBase(UUID uuid) {
        return base.getOrDefault(uuid, DEFAULT_BASE);
    }

    /** 기본 최대 체력을 변경하고 즉시 재계산 */
    public void setBase(UUID uuid, double value) {
        base.put(uuid, value);
        Player player = Asurajang.getInstance().getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) recalculate(player);
    }

    // ── 재계산 ───────────────────────────────────────────────────────────────

    /**
     * 활성 증강의 MaxHealthModifier를 모두 읽어 최종 최대 체력을 계산하고 적용한다.
     *
     * 우선순위:
     *   1. Fixed 중 가장 낮은 fraction × base → effectiveBase
     *   2. Fixed가 없으면 base 그대로
     *   3. 모든 Multiplier를 effectiveBase에 곱셈 적용
     *   4. 모든 Additive를 더해 최종 적용
     */
    public void recalculate(Player player) {
        double baseHp = getBase(player.getUniqueId());

        double fixedMin = Double.MAX_VALUE;
        double totalMul = 1.0;
        double totalAdd = 0.0;

        for (AugmentationEffect effect :
                Asurajang.getInstance().getAugmentationManager()
                         .getActiveEffects(player.getUniqueId()).values()) {
            MaxHealthModifier mod = effect.getMaxHealthModifier();
            if (mod instanceof MaxHealthModifier.Fixed f) {
                fixedMin = Math.min(fixedMin, f.fraction());
            } else if (mod instanceof MaxHealthModifier.Multiplier m) {
                totalMul *= m.factor();
            } else if (mod instanceof MaxHealthModifier.Additive a) {
                totalAdd += a.amount();
            }
        }

        double effectiveBase = (fixedMin < Double.MAX_VALUE) ? baseHp * fixedMin : baseHp;
        double finalHp = Math.max(1.0, effectiveBase * totalMul + totalAdd);

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        attr.setBaseValue(finalHp);
        if (player.getHealth() > finalHp) player.setHealth(finalHp);
    }
}
