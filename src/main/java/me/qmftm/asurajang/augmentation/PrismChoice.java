package me.qmftm.asurajang.augmentation;

import org.bukkit.inventory.ItemStack;

public sealed interface PrismChoice permits PrismChoice.Aug, PrismChoice.Item {

    ItemStack icon();

    record Aug(Augmentation augmentation) implements PrismChoice {
        public ItemStack icon() { return augmentation.getIcon().clone(); }
    }

    record Item(ItemStack stack) implements PrismChoice {
        public ItemStack icon() { return stack.clone(); }
    }
}
