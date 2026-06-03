package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.GyeongjeongwonEffect;
import me.qmftm.asurajang.augmentation.effect.HeugsomEffect;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;

public class AugmentationEffectListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();
        if (killer == null) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(killer.getUniqueId()).values())) {
            effect.onKillEnemy(killer, victim);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getSourceEntity() instanceof Player attacker)) return;
        if (HeugsomEffect.pendingKnockback.remove(attacker.getUniqueId())) {
            Vector kb = event.getFinalKnockback();
            event.setFinalKnockback(new Vector(kb.getX() * 4.0, kb.getY(), kb.getZ() * 4.0));
        } else if (GyeongjeongwonEffect.pendingDoubleKnockback.remove(attacker.getUniqueId())) {
            event.setFinalKnockback(event.getFinalKnockback().multiply(2.0));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (!(event.getDamager() instanceof Projectile proj)) return;
        if (!(proj.getShooter() instanceof Player shooter)) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(shooter.getUniqueId()).values())) {
            effect.onProjectileDamageAsAttacker(shooter, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(attacker.getUniqueId()).values())) {
            effect.onDamageAsAttacker(attacker, event);
        }
    }

    // HotbarButtonListener(LOW)가 먼저 실행되어 상점/증강 버튼을 취소하므로
    // ignoreCancelled = true 로 취소된 이벤트는 무시
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(player.getUniqueId()).values())) {
            effect.onRightClick(player, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(player.getUniqueId()).values())) {
            effect.onSwapHands(player, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (Asurajang.getInstance().getAugmentationManager()
                .getActiveEffects(player.getUniqueId()).containsKey("lightlanding")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        Asurajang.getInstance().getAugmentationManager().deactivateFor(event.getPlayer());
    }
}
