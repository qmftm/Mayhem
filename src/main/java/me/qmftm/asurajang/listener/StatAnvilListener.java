package me.qmftm.asurajang.listener;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.gui.StatAnvilGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class StatAnvilListener implements Listener {

    private static final String[]    MODIFIER_KEYS  = {"stat_anvil_attack", "stat_anvil_defense", "stat_anvil_speed"};
    private static final Attribute[] MODIFIER_ATTRS = {Attribute.ATTACK_DAMAGE, Attribute.ARMOR, Attribute.MOVEMENT_SPEED};

    @EventHandler(priority = EventPriority.LOW)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!Asurajang.getInstance().getGameManager().isRunning()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().getPersistentDataContainer()
                .has(Asurajang.STAT_ANVIL_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        new StatAnvilGUI().open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof StatAnvilGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        StatAnvilGUI.Stat stat = gui.getStatAt(event.getRawSlot());
        if (stat == null) return;

        player.closeInventory();
        applyStat(player, stat);
        player.sendMessage(Component.text("[" + stat.getDisplayName() + "] 능력치를 강화했습니다.", NamedTextColor.LIGHT_PURPLE));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.2f);
    }

    private void applyStat(Player player, StatAnvilGUI.Stat stat) {
        switch (stat) {
            case ATTACK     -> addModifier(player, Attribute.ATTACK_DAMAGE,  "stat_anvil_attack",  2.0,  AttributeModifier.Operation.ADD_NUMBER);
            case DEFENSE    -> addModifier(player, Attribute.ARMOR,           "stat_anvil_defense", 1.0,  AttributeModifier.Operation.ADD_NUMBER);
            case MAX_HEALTH -> {
                var mgr = Asurajang.getInstance().getMaxHealthManager();
                mgr.setBase(player.getUniqueId(), mgr.getBase(player.getUniqueId()) + 4.0);
            }
            case SPEED      -> addModifier(player, Attribute.MOVEMENT_SPEED, "stat_anvil_speed",   0.01, AttributeModifier.Operation.ADD_NUMBER);
        }
    }

    private void addModifier(Player player, Attribute attribute, String keyId,
                             double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        NamespacedKey key = new NamespacedKey(Asurajang.getInstance(), keyId);
        double prev = inst.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .mapToDouble(AttributeModifier::getAmount)
            .sum();
        inst.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .toList()
            .forEach(inst::removeModifier);
        inst.addModifier(new AttributeModifier(key, prev + amount, op));
    }

    public void cleanup(Player player) {
        for (int i = 0; i < MODIFIER_KEYS.length; i++) {
            AttributeInstance inst = player.getAttribute(MODIFIER_ATTRS[i]);
            if (inst == null) continue;
            NamespacedKey key = new NamespacedKey(Asurajang.getInstance(), MODIFIER_KEYS[i]);
            inst.getModifiers().stream()
                .filter(m -> m.getKey().equals(key))
                .toList()
                .forEach(inst::removeModifier);
        }
    }
}
