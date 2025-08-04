package de.tomalbrc.filamentcosmetics.gui.actions;


import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import de.tomalbrc.filamentcosmetics.gui.ColorPickerComponent;
import de.tomalbrc.filamentcosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenColorPickerAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        new ColorPickerComponent(player, entry.itemStack(), (coloredStack) -> {
            ((ICosmetics) player).getHatCosmetic().equip(coloredStack);
        }).open();
    }
}
