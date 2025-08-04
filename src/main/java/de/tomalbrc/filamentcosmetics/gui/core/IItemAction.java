package de.tomalbrc.filamentcosmetics.gui.core;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface IItemAction {
    void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui);
}
