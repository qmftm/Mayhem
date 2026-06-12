package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import me.qmftm.asurajang.augmentation.effect.AugmentationEffect;
import me.qmftm.asurajang.augmentation.effect.DivergentFistEffect;
import me.qmftm.asurajang.augmentation.effect.BlackFlashEffect;
import me.qmftm.asurajang.augmentation.effect.DropkickEffect;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.qmftm.asurajang.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
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
        if (BlackFlashEffect.pendingKnockback.remove(attacker.getUniqueId())) {
            double mult = AugmentSettings.getDouble("BlackFlash", "knockback-multiplier", 4.0);
            Vector kb = event.getFinalKnockback();
            event.setFinalKnockback(new Vector(kb.getX() * mult, kb.getY(), kb.getZ() * mult));
        } else if (DivergentFistEffect.pendingDoubleKnockback.remove(attacker.getUniqueId())) {
            double mult = AugmentSettings.getDouble("DivergentFist", "knockback-multiplier", 2.0);
            Vector kb = event.getFinalKnockback();
            event.setFinalKnockback(new Vector(kb.getX() * mult, kb.getY(), kb.getZ() * mult));
        } else if (DropkickEffect.pendingKnockback.remove(attacker.getUniqueId())) {
            double mult = AugmentSettings.getDouble("Dropkick", "knockback-multiplier", 5.0);
            Vector kb = event.getFinalKnockback();
            event.setFinalKnockback(new Vector(kb.getX() * mult, kb.getY() + 0.4, kb.getZ() * mult));
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
        java.util.Map<String, AugmentationEffect> effects = mgr.getActiveEffects(attacker.getUniqueId());

        // 경정권을 먼저 처리해 이번 타격에서 실제로 발동했는지 확인
        // (쿨타임 중이라 발동하지 않았다면 흑섬은 평소처럼 터질 수 있음)
        AugmentationEffect divergentFist = effects.get("DivergentFist");
        boolean blockHeugsom = false;
        if (divergentFist != null) {
            divergentFist.onDamageAsAttacker(attacker, event);
            blockHeugsom = DivergentFistEffect.activatedOnThisHit.remove(attacker.getUniqueId());
        }

        for (AugmentationEffect effect : new ArrayList<>(effects.values())) {
            if (effect == divergentFist) continue; // 위에서 이미 처리함
            if (blockHeugsom && effect instanceof BlackFlashEffect) continue;
            effect.onDamageAsAttacker(attacker, event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (!event.getAction().isRightClick()) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(player.getUniqueId()).values())) {
            effect.onRightClick(player, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(player.getUniqueId()).values())) {
            effect.onInteractEntity(player, event);
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
    public void onDropItem(PlayerDropItemEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        Player player = event.getPlayer();
        AugmentationManager mgr = Asurajang.getInstance().getAugmentationManager();
        for (AugmentationEffect effect : new ArrayList<>(mgr.getActiveEffects(player.getUniqueId()).values())) {
            effect.onDropItem(player, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (Asurajang.getInstance().getAugmentationManager()
                .getActiveEffects(player.getUniqueId()).containsKey("FeatherFalling")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        Asurajang.getInstance().getAugmentationManager().deactivateFor(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTeamDamage(EntityDamageByEntityEvent event) {
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;
        if (Asurajang.getInstance().getGameManager().getGameMode() != GameManager.GameMode.TEAM) return;
        int attackerTeam = getEntityTeam(event.getDamager());
        if (attackerTeam == -1) return;
        int victimTeam = getEntityTeam(event.getEntity());
        if (victimTeam == -1) return;
        if (attackerTeam == victimTeam) event.setCancelled(true);
    }

    private static int getEntityTeam(Entity entity) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        if (entity instanceof Player p) return gm.getTeam(p.getUniqueId());
        if (entity instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            return gm.getTeam(p.getUniqueId());
        }
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        String entry = entity.getUniqueId().toString();
        Team red  = main.getTeam("mayhem_red");
        Team blue = main.getTeam("mayhem_blue");
        if (red  != null && red.hasEntry(entry))  return 0;
        if (blue != null && blue.hasEntry(entry)) return 1;
        return -1;
    }
}
