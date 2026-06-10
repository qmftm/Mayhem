package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.MaxHealthModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GlassCannonEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public MaxHealthModifier getMaxHealthModifier() {
        return new MaxHealthModifier.Multiplier(AugmentSettings.getDouble("GlassCannon", "max-health-multiplier", 0.7));
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        double extra = event.getDamage() * AugmentSettings.getDouble("GlassCannon", "extra-damage-ratio", 0.15);

        // 방어력 계산이 끝난 다음 틱에 체력을 직접 깎아 방어력을 무시하는 고정 피해로 적용
        // (이번 이벤트가 팀 피해 등으로 취소되면 함께 무효화되도록 취소 여부를 다시 확인)
        Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> {
            if (event.isCancelled() || victim.isDead() || !victim.isValid()) return;
            victim.setHealth(Math.max(0.0, victim.getHealth() - extra));
        });
    }
}
