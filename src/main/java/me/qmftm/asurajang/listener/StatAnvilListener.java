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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StatAnvilListener implements Listener {

    private static final String KEY_ATTACK = "stat_anvil_attack";
    private static final String KEY_SPEED  = "stat_anvil_speed";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof StatAnvilGUI gui)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        StatAnvilGUI.Stat stat = gui.getStatAt(event.getRawSlot());
        if (stat == null) return;

        if (!Asurajang.getInstance().getLevelUpManager().consumeAnvilCharge(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        player.closeInventory();
        applyStat(player, stat);
        player.sendMessage(Component.text("[" + stat.getDisplayName() + "] 능력치를 강화했습니다.", NamedTextColor.LIGHT_PURPLE));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.2f);
    }

    private void applyStat(Player player, StatAnvilGUI.Stat stat) {
        var mgr = Asurajang.getInstance().getMaxHealthManager();
        mgr.setBase(player.getUniqueId(), mgr.getBase(player.getUniqueId()) + stat.getHpBonus());
        addModifier(player, Attribute.ATTACK_DAMAGE,  KEY_ATTACK, stat.getAttackBonus());
        addModifier(player, Attribute.MOVEMENT_SPEED, KEY_SPEED,  stat.getSpeedBonus());
    }

    private void addModifier(Player player, Attribute attribute, String keyId, double amount) {
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
        inst.addModifier(new AttributeModifier(key, prev + amount, AttributeModifier.Operation.ADD_NUMBER));
    }

    public void cleanup(Player player) {
        removeModifier(player, Attribute.ATTACK_DAMAGE,  KEY_ATTACK);
        removeModifier(player, Attribute.MOVEMENT_SPEED, KEY_SPEED);
        AttributeInstance hp = player.getAttribute(Attribute.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(20.0);
    }

    private void removeModifier(Player player, Attribute attribute, String keyId) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        NamespacedKey key = new NamespacedKey(Asurajang.getInstance(), keyId);
        inst.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .toList()
            .forEach(inst::removeModifier);
    }
}
