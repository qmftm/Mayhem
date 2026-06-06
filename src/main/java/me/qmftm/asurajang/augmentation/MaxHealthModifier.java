package me.qmftm.asurajang.augmentation;

public sealed interface MaxHealthModifier
        permits MaxHealthModifier.Fixed, MaxHealthModifier.Multiplier {

    /** 기본 최대 체력의 fraction 배로 고정. 여러 Fixed가 있으면 가장 낮은 값 우선. */
    record Fixed(double fraction) implements MaxHealthModifier {}

    /** 최종 체력에 곱셈 적용 (0.7 = -30%, 1.3 = +30%) */
    record Multiplier(double factor) implements MaxHealthModifier {}
}
