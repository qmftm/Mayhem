package me.qmftm.asurajang.augmentation;

import org.bukkit.inventory.ItemStack;

public class Augmentation {

    private final String id;
    private final String displayName;
    private final ItemStack icon;
    private final boolean prism;
    private final boolean active;
    private final int cooldown;
    private final boolean cooldownOnUse;

    public Augmentation(String id, String displayName, ItemStack icon, boolean prism, boolean active, int cooldown, boolean cooldownOnUse) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.prism = prism;
        this.active = active;
        this.cooldown = cooldown;
        this.cooldownOnUse = cooldownOnUse;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ItemStack getIcon() { return icon; }
    public boolean isPrism() { return prism; }
    public boolean isActive() { return active; }
    public int getCooldown() { return cooldown; }
    public boolean isCooldownOnUse() { return cooldownOnUse; }
}
