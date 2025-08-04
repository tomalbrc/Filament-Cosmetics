package de.tomalbrc.filamentcosmetics.gui.providers;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.gui.core.ICosmeticProvider;
import java.util.List;
import net.minecraft.world.item.Item;

public class ItemSkinProvider implements ICosmeticProvider {
    private final Item targetItem;

    public ItemSkinProvider(Item targetItem) {
        this.targetItem = targetItem;
    }

    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getAllCosmeticsForMaterial(ItemType.ITEM_SKIN, targetItem);
    }
}
