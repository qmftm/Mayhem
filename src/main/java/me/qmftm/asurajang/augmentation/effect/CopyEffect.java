package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CopyEffect implements AugmentationEffect {

    private int maxCopies;
    private int remainingCopies;

    @Override
    public void onActivate(Player player) {
        maxCopies = AugmentSettings.getInt("Copy", "max-copies", 3);
        remainingCopies = maxCopies;
    }

    @Override public void onDeactivate(Player player) {}

    @Override
    public void onKillEnemy(Player player, Player victim) {
        if (remainingCopies <= 0) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        Set<String> mine   = mgr.getActiveEffects(player.getUniqueId()).keySet();
        Set<String> theirs = mgr.getActiveEffects(victim.getUniqueId()).keySet();

        if (theirs.isEmpty()) return;

        List<String> notHave = new ArrayList<>();
        for (String id : theirs) {
            if (!mine.contains(id) && !id.equals("Copy")) notHave.add(id);
        }

        if (notHave.isEmpty()) return;

        String toGet = notHave.get(ThreadLocalRandom.current().nextInt(notHave.size()));
        remainingCopies--;

        if (remainingCopies <= 0) {
            mgr.deactivateSingle(player, "Copy");
        }

        mgr.activateFor(player, toGet);

        String augName = mgr.get(toGet) != null ? mgr.get(toGet).getDisplayName() : toGet;
        player.sendMessage(Component.text("[모방] ", NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(augName + " 증강을 복사했습니다.", NamedTextColor.GRAY))
            .append(Component.text(" (남은 모방 횟수: " + remainingCopies + "/" + maxCopies + ")", NamedTextColor.DARK_GRAY)));
        player.playSound(player.getLocation(), Sound.ENTITY_SLIME_ATTACK, 1.0f, 1.0f);
    }
}
