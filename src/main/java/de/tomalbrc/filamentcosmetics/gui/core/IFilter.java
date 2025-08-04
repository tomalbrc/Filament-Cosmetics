package de.tomalbrc.filamentcosmetics.gui.core;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IFilter {
    boolean test(ServerPlayerEntity player, CustomItemEntry entry);
}
