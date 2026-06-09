package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.Augmentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PrismAugItemListener implements Listener {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

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

        // cooldown-on-use: false → 소모 아이템이 깨질 때 쿨타임 시작. 이미 보유 중이면 지급 불가.
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
        if (aug == null) return;

        cooldowns.computeIfAbsent(event.getPlayer().getUniqueId(), k -> new HashMap<>())
            .put(augId, System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
