package de.tomalbrc.filamentcosmetics.gui.core;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;

import java.util.List;

@FunctionalInterface
public interface ICosmeticProvider {
    List<CustomItemEntry> getItems();
}