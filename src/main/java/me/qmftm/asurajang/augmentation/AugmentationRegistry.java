package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.BoogieWoogieEffect;
import me.qmftm.asurajang.augmentation.effect.ByeontaeEffect;
import me.qmftm.asurajang.augmentation.effect.DeadfulEffect;
import me.qmftm.asurajang.augmentation.effect.GaebokChiEffect;
import me.qmftm.asurajang.augmentation.effect.CleanupEffect;
import me.qmftm.asurajang.augmentation.effect.GlassCannonEffect;
import me.qmftm.asurajang.augmentation.effect.GuiltPleasureEffect;
import me.qmftm.asurajang.augmentation.effect.GyeongjeongwonEffect;
import me.qmftm.asurajang.augmentation.effect.HeavyForceEffect;
import me.qmftm.asurajang.augmentation.effect.HeugsomEffect;
import me.qmftm.asurajang.augmentation.effect.LightLandingEffect;
import me.qmftm.asurajang.augmentation.effect.MobangEffect;
import me.qmftm.asurajang.augmentation.effect.NaengnyeolhanEffect;
import me.qmftm.asurajang.augmentation.effect.SelfDestructEffect;
import me.qmftm.asurajang.augmentation.effect.SniperDuelEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AugmentationRegistry {

    private static final Map<String, Supplier<AugmentationEffect>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("heuksom",      HeugsomEffect::new);
        REGISTRY.put("deadful",      DeadfulEffect::new);
        REGISTRY.put("boogiewoogie", BoogieWoogieEffect::new);
        REGISTRY.put("gaebokchi",      GaebokChiEffect::new);
        REGISTRY.put("byeontae",       ByeontaeEffect::new);
        REGISTRY.put("mobang",         MobangEffect::new);
        REGISTRY.put("sniperduel",     SniperDuelEffect::new);
        REGISTRY.put("gyeongjeongwon", GyeongjeongwonEffect::new);
        REGISTRY.put("naengnyeolhan",  NaengnyeolhanEffect::new);
        REGISTRY.put("heavyforce",     HeavyForceEffect::new);
        REGISTRY.put("selfdestruct",   SelfDestructEffect::new);
        REGISTRY.put("guiltpleasure",  GuiltPleasureEffect::new);
        REGISTRY.put("glasscannon",    GlassCannonEffect::new);
        REGISTRY.put("cleanup",        CleanupEffect::new);
        REGISTRY.put("lightlanding",   LightLandingEffect::new);
    }

    @Nullable
    public static AugmentationEffect create(String id) {
        Supplier<AugmentationEffect> factory = REGISTRY.get(id);
        return factory != null ? factory.get() : null;
    }
}
