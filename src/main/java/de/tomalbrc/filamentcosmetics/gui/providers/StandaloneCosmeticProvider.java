package de.tomalbrc.filamentcosmetics.gui.providers;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.gui.core.ICosmeticProvider;

import java.util.List;
import java.util.stream.Stream;

public class StandaloneCosmeticProvider implements ICosmeticProvider {

//    public final ItemType type;
//    public StandaloneCosmeticProvider(ItemType type) {
//        this.type = type;
//    }
//
    @Override
    public List<CustomItemEntry> getItems() {
        return Stream.concat(CustomItemRegistry.getAllCosmetics(ItemType.HAT).stream(), CustomItemRegistry.getAllCosmetics(ItemType.BODY_COSMETIC).stream()).toList();
    }
}
