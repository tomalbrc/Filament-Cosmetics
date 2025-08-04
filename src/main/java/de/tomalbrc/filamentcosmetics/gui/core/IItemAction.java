package de.tomalbrc.filamentcosmetics.gui.core;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface IItemAction {
    void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui);
}
