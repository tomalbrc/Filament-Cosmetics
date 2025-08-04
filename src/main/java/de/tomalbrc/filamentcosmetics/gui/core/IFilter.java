package de.tomalbrc.filamentcosmetics.gui.core;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import net.minecraft.server.level.ServerPlayer;

public interface IFilter {
    boolean test(ServerPlayer player, CustomItemEntry entry);
}
