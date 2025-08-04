package de.tomalbrc.filamentcosmetics.gui.actions;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import de.tomalbrc.filamentcosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EquipCosmeticAction implements IItemAction {
    @Override
    public void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui) {
        gui.close();
        execute(player, entry.itemStack(), entry.type());
    }

    public void execute(ServerPlayer player, ItemStack cosmeticStack, ItemType type) {
        if(type == ItemType.HAT) {
            ((ICosmetics) player).getHatCosmetic().equip(cosmeticStack);
        } else if(type == ItemType.BODY_COSMETIC) {
            ((ICosmetics) player).getBodyCosmetics().equip(cosmeticStack);
        }
    }
}