package me.qmftm.asurajang.augmentation;

import org.bukkit.inventory.ItemStack;

public class Augmentation {

    private final String id;
    private final String displayName;
    private final ItemStack icon;

    public Augmentation(String id, String displayName, ItemStack icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ItemStack getIcon() { return icon; }
}
