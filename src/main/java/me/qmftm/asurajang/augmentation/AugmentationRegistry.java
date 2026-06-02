package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.BoogieWoogieEffect;
import me.qmftm.asurajang.augmentation.effect.ByeontaeEffect;
import me.qmftm.asurajang.augmentation.effect.DeadfulEffect;
import me.qmftm.asurajang.augmentation.effect.GaebokChiEffect;
import me.qmftm.asurajang.augmentation.effect.HeugsomEffect;
import me.qmftm.asurajang.augmentation.effect.MobangEffect;
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
        REGISTRY.put("gaebokchi",    GaebokChiEffect::new);
        REGISTRY.put("byeontae",     ByeontaeEffect::new);
        REGISTRY.put("mobang",       MobangEffect::new);
    }

    @Nullable
    public static AugmentationEffect create(String id) {
        Supplier<AugmentationEffect> factory = REGISTRY.get(id);
        return factory != null ? factory.get() : null;
    }
}
