package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.Augmentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PrismAugItemListener implements Listener {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, BukkitTask>> respawnTasks = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String augId = meta.getPersistentDataContainer().get(Asurajang.PRISM_AUG_KEY, PersistentDataType.STRING);
        if (augId == null) return;

        event.setCancelled(true);

        Augmentation aug = Asurajang.getInstance().getAugmentationManager().get(augId);
        long cooldownMs = aug != null ? aug.getCooldown() * 1000L : 30_000L;

        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(augId, 0L);
        long remaining = cooldownMs - (now - lastUse);

        if (remaining > 0) {
            player.sendMessage(Component.text("쿨타임: " + (remaining / 1000 + 1) + "초 남았습니다.", NamedTextColor.RED));
            return;
        }

        if (aug != null && !aug.isCooldownOnUse()) {
            boolean hasConsumable = Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .anyMatch(i -> {
                    ItemMeta m = i.getItemMeta();
                    return m != null && augId.equals(
                        m.getPersistentDataContainer().get(Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING));
                });
            if (hasConsumable) {
                player.sendMessage(Component.text("이미 보유 중입니다.", NamedTextColor.RED));
                return;
            }
        }

        if (aug != null && aug.isCooldownOnUse()) {
            cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(augId, now);
        }

        Asurajang.getInstance().getAugmentationManager().activateFor(player, augId);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.2f);
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemMeta meta = event.getBrokenItem().getItemMeta();
        if (meta == null) return;

        String augId = meta.getPersistentDataContainer().get(Asurajang.CONSUMABLE_AUG_KEY, PersistentDataType.STRING);
        if (augId == null) return;

        Augmentation aug = Asurajang.getInstance().getAugmentationManager().get(augId);
        int cooldownSec = aug != null ? aug.getCooldown() : 30;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        player.sendMessage(Component.text(
            "[처형인의 검] " + cooldownSec + "초 후에 다시 지급됩니다.", NamedTextColor.GRAY));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Asurajang.getInstance(), () -> {
            respawnTasks.getOrDefault(uuid, Map.of()).remove(augId);
            if (!player.isOnline()) return;
            Asurajang.getInstance().getAugmentationManager().activateFor(player, augId);
            player.sendMessage(Component.text("[처형인의 검] 지급되었습니다.", NamedTextColor.YELLOW));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.2f);
        }, cooldownSec * 20L);

        respawnTasks.computeIfAbsent(uuid, k -> new HashMap<>()).put(augId, task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    // 게임 종료/플레이어 퇴장 시 예약된 재지급 작업을 취소해
    // 게임이 끝난 뒤에 처형인의 검 등이 지급되는 것을 방지
    public void cleanup(UUID uuid) {
        cooldowns.remove(uuid);
        Map<String, BukkitTask> tasks = respawnTasks.remove(uuid);
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);
    }
}
