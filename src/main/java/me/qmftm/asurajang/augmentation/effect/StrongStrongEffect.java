package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class StrongStrongEffect implements AugmentationEffect {

    @Override
    public void onActivate(Player player) {}

    @Override
    public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        UUID topKillerUuid = null;
        int maxKills = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            int k = Asurajang.getInstance().getScoreboardManager().getKills(p.getUniqueId());
            if (k > maxKills) {
                maxKills = k;
                topKillerUuid = p.getUniqueId();
            }
        }

        if (maxKills > 0 && victim.getUniqueId().equals(topKillerUuid)) {
            event.setDamage(event.getDamage() * AugmentSettings.getDouble("StrongStrong", "damage-multiplier", 1.5));
        }
    }
}
