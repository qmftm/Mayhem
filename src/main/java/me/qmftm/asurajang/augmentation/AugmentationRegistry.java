package me.qmftm.asurajang.augmentation;

import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.BoogieWoogieEffect;
import me.qmftm.asurajang.augmentation.effect.UnBreakableEffect;
import me.qmftm.asurajang.augmentation.effect.UnRuinEffect;
import me.qmftm.asurajang.augmentation.effect.SaturatedFatEffect;
import me.qmftm.asurajang.augmentation.effect.GamblerEffect;
import me.qmftm.asurajang.augmentation.effect.DivineLoveEffect;
import me.qmftm.asurajang.augmentation.effect.WindBurstEffect;
import me.qmftm.asurajang.augmentation.effect.StrongStrongEffect;
import me.qmftm.asurajang.augmentation.effect.StrongWeakEffect;
import me.qmftm.asurajang.augmentation.effect.HentaiEffect;
import me.qmftm.asurajang.augmentation.effect.DeadfulEffect;
import me.qmftm.asurajang.augmentation.effect.MolaMolaEffect;
import me.qmftm.asurajang.augmentation.effect.CleanUpEffect;
import me.qmftm.asurajang.augmentation.effect.GlassCannonEffect;
import me.qmftm.asurajang.augmentation.effect.GuiltPleasureEffect;
import me.qmftm.asurajang.augmentation.effect.DivergentFistEffect;
import me.qmftm.asurajang.augmentation.effect.HeavyForceEffect;
import me.qmftm.asurajang.augmentation.effect.BlackFlashEffect;
import me.qmftm.asurajang.augmentation.effect.FeatherFallingEffect;
import me.qmftm.asurajang.augmentation.effect.CopyEffect;
import me.qmftm.asurajang.augmentation.effect.ColdBloodEffect;
import me.qmftm.asurajang.augmentation.effect.DivineDogsEffect;
import me.qmftm.asurajang.augmentation.effect.SelfBomberEffect;
import me.qmftm.asurajang.augmentation.effect.DropkickEffect;
import me.qmftm.asurajang.augmentation.effect.ExecutionerSwordEffect;
import me.qmftm.asurajang.augmentation.effect.StatPlusPlusPlusEffect;
import me.qmftm.asurajang.augmentation.effect.TabDancerEffect;
import me.qmftm.asurajang.augmentation.effect.SniperDuelEffect;
import me.qmftm.asurajang.augmentation.effect.GrabEffect;
import me.qmftm.asurajang.augmentation.effect.BodyBombEffect;
import me.qmftm.asurajang.augmentation.effect.CharmEffect;
import me.qmftm.asurajang.augmentation.effect.InfiniteGoldEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AugmentationRegistry {

    private static final Map<String, Supplier<AugmentationEffect>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("BlackFlash",    BlackFlashEffect::new);
        REGISTRY.put("Deadful",       DeadfulEffect::new);
        REGISTRY.put("BoogieWoogie",  BoogieWoogieEffect::new);
        REGISTRY.put("MolaMola",      MolaMolaEffect::new);
        REGISTRY.put("Hentai",        HentaiEffect::new);
        REGISTRY.put("Copy",          CopyEffect::new);
        REGISTRY.put("SniperDuel",    SniperDuelEffect::new);
        REGISTRY.put("DivergentFist", DivergentFistEffect::new);
        REGISTRY.put("ColdBlood",     ColdBloodEffect::new);
        REGISTRY.put("HeavyForce",    HeavyForceEffect::new);
        REGISTRY.put("SelfBomber",    SelfBomberEffect::new);
        REGISTRY.put("GuiltPleasure", GuiltPleasureEffect::new);
        REGISTRY.put("GlassCannon",   GlassCannonEffect::new);
        REGISTRY.put("CleanUp",       CleanUpEffect::new);
        REGISTRY.put("FeatherFalling",FeatherFallingEffect::new);
        REGISTRY.put("DivineDogs",    DivineDogsEffect::new);
        REGISTRY.put("StrongWeak",    StrongWeakEffect::new);
        REGISTRY.put("StrongStrong",  StrongStrongEffect::new);
        REGISTRY.put("WindBurst",     WindBurstEffect::new);
        REGISTRY.put("DivineLove",    DivineLoveEffect::new);
        REGISTRY.put("UnBreakable",   UnBreakableEffect::new);
        REGISTRY.put("UnRuin",        UnRuinEffect::new);
        REGISTRY.put("SaturatedFat",  SaturatedFatEffect::new);
        REGISTRY.put("Gambler",       GamblerEffect::new);
        REGISTRY.put("Dropkick",          DropkickEffect::new);
        REGISTRY.put("ExecutionerSword",  ExecutionerSwordEffect::new);
        REGISTRY.put("StatPlusPlusPlus",  StatPlusPlusPlusEffect::new);
        REGISTRY.put("TabDancer",         TabDancerEffect::new);
        REGISTRY.put("Grab",              GrabEffect::new);
        REGISTRY.put("BodyBomb",          BodyBombEffect::new);
        REGISTRY.put("Charm",             CharmEffect::new);
        REGISTRY.put("InfiniteGold",      InfiniteGoldEffect::new);
    }

    @Nullable
    public static AugmentationEffect create(String id) {
        Supplier<AugmentationEffect> factory = REGISTRY.get(id);
        return factory != null ? factory.get() : null;
    }
}
