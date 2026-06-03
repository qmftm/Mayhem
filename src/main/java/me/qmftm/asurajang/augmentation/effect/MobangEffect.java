package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MobangEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onKillEnemy(Player player, Player victim) {
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        Set<String> mine   = mgr.getActiveEffects(player.getUniqueId()).keySet();
        Set<String> theirs = mgr.getActiveEffects(victim.getUniqueId()).keySet();

        if (theirs.isEmpty()) return;

        // 내가 없는 증강 우선
        List<String> notHave = new ArrayList<>();
        for (String id : theirs) {
            if (!mine.contains(id)) notHave.add(id);
        }

        if (notHave.isEmpty()) return; // 전부 동일 → 효과 없음

        String toGet = notHave.get(ThreadLocalRandom.current().nextInt(notHave.size()));
        mgr.deactivateSingle(player, "mobang");
        mgr.activateFor(player, toGet);

        String augName = mgr.get(toGet) != null ? mgr.get(toGet).getDisplayName() : toGet;
        player.sendMessage(Component.text("[모방] ", NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(augName + " 증강을 복사했습니다.", NamedTextColor.GRAY)));
        player.playSound(player.getLocation(), Sound.ENTITY_SLIME_ATTACK, 1.0f, 1.0f);
    }
}
