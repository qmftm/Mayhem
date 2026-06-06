package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.BoogieWoogieEffect;
import me.qmftm.asurajang.augmentation.effect.GanggangYakyakEffect;
import me.qmftm.asurajang.augmentation.effect.GangyakYakgangEffect;
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
import me.qmftm.asurajang.augmentation.effect.OkgyeonEffect;
import me.qmftm.asurajang.augmentation.effect.SelfDestructEffect;
import me.qmftm.asurajang.augmentation.effect.SniperDuelEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AugmentationRegistry {

    private static final Map<String, Supplier<AugmentationEffect>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("blackflash",    HeugsomEffect::new);
        REGISTRY.put("deadful",       DeadfulEffect::new);
        REGISTRY.put("boogiewoogie",  BoogieWoogieEffect::new);
        REGISTRY.put("molamola",      GaebokChiEffect::new);
        REGISTRY.put("hentai",        ByeontaeEffect::new);
        REGISTRY.put("copy",          MobangEffect::new);
        REGISTRY.put("sniperduel",    SniperDuelEffect::new);
        REGISTRY.put("divergentfist", GyeongjeongwonEffect::new);
        REGISTRY.put("coldblood",     NaengnyeolhanEffect::new);
        REGISTRY.put("heavyforce",    HeavyForceEffect::new);
        REGISTRY.put("selfbomber",    SelfDestructEffect::new);
        REGISTRY.put("guiltpleasure", GuiltPleasureEffect::new);
        REGISTRY.put("glasscannon",   GlassCannonEffect::new);
        REGISTRY.put("cleanup",       CleanupEffect::new);
        REGISTRY.put("featherfalling",LightLandingEffect::new);
        REGISTRY.put("okgyeon",        OkgyeonEffect::new);
        REGISTRY.put("gangyakyakgang", GangyakYakgangEffect::new);
        REGISTRY.put("ganggangyakyak", GanggangYakyakEffect::new);
    }

    @Nullable
    public static AugmentationEffect create(String id) {
        Supplier<AugmentationEffect> factory = REGISTRY.get(id);
        return factory != null ? factory.get() : null;
    }
}
