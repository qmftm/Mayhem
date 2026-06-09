package me.qmftm.asurajang.augmentation;

import org.bukkit.inventory.ItemStack;

public class Augmentation {

    private final String id;
    private final String displayName;
    private final ItemStack icon;
    private final boolean prism;
    private final boolean active;

    public Augmentation(String id, String displayName, ItemStack icon, boolean prism, boolean active) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.prism = prism;
        this.active = active;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ItemStack getIcon() { return icon; }
    public boolean isPrism() { return prism; }
    public boolean isActive() { return active; }
}
