package de.tomalbrc.filamentcosmetics.gui.actions;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public class ApplySkinAction implements IItemAction {
    private final ItemStack targetItemStack;
    private final int itemDisplaySlot;

    public ApplySkinAction(ItemStack targetItemStack, int itemDisplaySlot) {
        this.targetItemStack = targetItemStack;
        this.itemDisplaySlot = itemDisplaySlot;
    }

    @Override
    public void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui) {
        // TODO: Filament skin component

        targetItemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, comp ->
                comp.update(nbt -> nbt.putString("cosmeticItemId", entry.id()))
        );
        targetItemStack.set(DataComponents.ITEM_MODEL, entry.itemStack().get(DataComponents.ITEM_MODEL));

        gui.setSlot(itemDisplaySlot, targetItemStack.copy());
    }
}
