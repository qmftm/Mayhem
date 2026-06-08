package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.augmentation.AugmentSettings;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class HentaiEffect implements AugmentationEffect {

    @Override public void onActivate(Player player) {}
    @Override public void onDeactivate(Player player) {}

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (ThreadLocalRandom.current().nextDouble() >= AugmentSettings.getDouble("Hentai", "strip-chance", 0.03)) return;

        PlayerInventory inv = target.getInventory();

        List<String> occupied = new ArrayList<>();
        if (hasItem(inv.getHelmet()))     occupied.add("head");
        if (hasItem(inv.getChestplate())) occupied.add("chest");
        if (hasItem(inv.getLeggings()))   occupied.add("legs");
        if (hasItem(inv.getBoots()))      occupied.add("feet");
        if (occupied.isEmpty()) return;

        String chosen = occupied.get(ThreadLocalRandom.current().nextInt(occupied.size()));
        ItemStack stripped = switch (chosen) {
            case "head"  -> { ItemStack i = inv.getHelmet();     inv.setHelmet(null);     yield i; }
            case "chest" -> { ItemStack i = inv.getChestplate(); inv.setChestplate(null); yield i; }
            case "legs"  -> { ItemStack i = inv.getLeggings();   inv.setLeggings(null);   yield i; }
            default      -> { ItemStack i = inv.getBoots();      inv.setBoots(null);      yield i; }
        };

        Map<Integer, ItemStack> leftover = inv.addItem(stripped);
        leftover.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));

        int heartCountBase = AugmentSettings.getInt("Hentai", "particle-count-base", 10);
        int heartCountRandom = AugmentSettings.getInt("Hentai", "particle-count-random", 6);
        int heartCount = heartCountBase + ThreadLocalRandom.current().nextInt(heartCountRandom); // 10~15개
        target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 1, 0),
            heartCount, 0.5, 0.5, 0.5, 0);

        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1.0f, 1.5f);
    }

    private static boolean hasItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
}
