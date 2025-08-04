package de.tomalbrc.filamentcosmetics.datafixer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class NbtDatafixer {
    private static final String OLD_NBT_KEY_ITEM_SKIN_ID = "itemSkinsID";
    private static final String NEW_NBT_KEY_CUSTOM_ITEM_ID = "cosmeticItemId";

    public static boolean fixItemStackNbt(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        CustomData customDataComponent = stack.get(DataComponents.CUSTOM_DATA);
        boolean modified = false;

        if (customDataComponent != null) {
            CompoundTag nbt = customDataComponent.copyTag();

            if (nbt.contains(OLD_NBT_KEY_ITEM_SKIN_ID, CompoundTag.TAG_STRING)) {
                if (!nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID, CompoundTag.TAG_STRING)) {
                    String idValue = nbt.getString(OLD_NBT_KEY_ITEM_SKIN_ID);
                    nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, idValue);
                }
                nbt.remove(OLD_NBT_KEY_ITEM_SKIN_ID);
                modified = true;

                if (modified) {
                    final CompoundTag finalNbt = nbt.copy();
                    stack.update(DataComponents.CUSTOM_DATA,
                            CustomData.EMPTY,
                            existing -> CustomData.of(finalNbt)
                    );
                }
            }
        }
        return modified;
    }
}
