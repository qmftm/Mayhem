package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.BoogieWoogieEffect;
import me.qmftm.asurajang.augmentation.effect.DivineLoveEffect;
import me.qmftm.asurajang.augmentation.effect.WindBurstEffect;
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
        REGISTRY.put("BlackFlash",    HeugsomEffect::new);
        REGISTRY.put("Deadful",       DeadfulEffect::new);
        REGISTRY.put("BoogieWoogie",  BoogieWoogieEffect::new);
        REGISTRY.put("MolaMola",      GaebokChiEffect::new);
        REGISTRY.put("Hentai",        ByeontaeEffect::new);
        REGISTRY.put("Copy",          MobangEffect::new);
        REGISTRY.put("SniperDuel",    SniperDuelEffect::new);
        REGISTRY.put("DivergentFist", GyeongjeongwonEffect::new);
        REGISTRY.put("ColdBlood",     NaengnyeolhanEffect::new);
        REGISTRY.put("HeavyForce",    HeavyForceEffect::new);
        REGISTRY.put("SelfBomber",    SelfDestructEffect::new);
        REGISTRY.put("GuiltPleasure", GuiltPleasureEffect::new);
        REGISTRY.put("GlassCannon",   GlassCannonEffect::new);
        REGISTRY.put("CleanUp",       CleanupEffect::new);
        REGISTRY.put("FeatherFalling",LightLandingEffect::new);
        REGISTRY.put("DivineDogs",    OkgyeonEffect::new);
        REGISTRY.put("StrongWeak",    GangyakYakgangEffect::new);
        REGISTRY.put("StrongStrong",  GanggangYakyakEffect::new);
        REGISTRY.put("WindBurst",     WindBurstEffect::new);
        REGISTRY.put("DivineLove",    DivineLoveEffect::new);
    }

    @Nullable
    public static AugmentationEffect create(String id) {
        Supplier<AugmentationEffect> factory = REGISTRY.get(id);
        return factory != null ? factory.get() : null;
    }
}
