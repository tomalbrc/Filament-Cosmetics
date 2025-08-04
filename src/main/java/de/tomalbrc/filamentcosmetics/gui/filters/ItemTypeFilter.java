package de.tomalbrc.filamentcosmetics.gui.filters;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;

import java.util.function.Predicate;

public record ItemTypeFilter(ItemType type) implements Predicate<CustomItemEntry> {
    @Override
    public boolean test(CustomItemEntry entry) {
        return entry.type() == this.type;
    }
}
