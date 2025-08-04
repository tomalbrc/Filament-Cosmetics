package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.datafixer.NbtDatafixer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    @Final
    @Shadow
    public Player player;
    @ModifyVariable(
            method = "setStack",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedSetStack(ItemStack stack){
        return checkItemSkinPermission(stack);
    }

    @ModifyVariable(
            method = "insertStack(ILnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedInsertStack(ItemStack stack) {
        return checkItemSkinPermission(stack);
    }

    @Unique
    public ItemStack checkItemSkinPermission(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        NbtDatafixer.fixItemStackNbt(stack);

        CustomData customDataComponent = stack.get(DataComponents.CUSTOM_DATA);

        if (customDataComponent != null) {
            CompoundTag nbt = customDataComponent.copyTag();
            if (nbt.contains("cosmeticItemId", CompoundTag.TAG_STRING)) {
                String itemSkinId = nbt.getString("cosmeticItemId");

                CustomItemEntry skinEntry = CustomItemRegistry.getCosmetic(itemSkinId);

                if (skinEntry == null) {
                    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, comp -> comp.update(currentNbt -> currentNbt.remove("cosmeticItemId")));
                    stack.remove(DataComponents.CUSTOM_MODEL_DATA);
                    return stack;
                }

                if (!Permissions.check(this.player, skinEntry.permission())) {
                    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, comp -> comp.update(currentNbt -> currentNbt.remove("cosmeticItemId")));
                    stack.remove(DataComponents.CUSTOM_MODEL_DATA);
                    return stack;
                }

                ItemStack skinDefinitionStack = skinEntry.itemStack();
                CustomModelData expectedModelData = skinDefinitionStack.get(DataComponents.CUSTOM_MODEL_DATA);
                CustomModelData currentModelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);

                if (expectedModelData != null) {
                    if (currentModelData == null || currentModelData.value() != expectedModelData.value()) {
                        stack.set(DataComponents.CUSTOM_MODEL_DATA, expectedModelData);
                    }
                } else {
                    stack.remove(DataComponents.CUSTOM_MODEL_DATA);
                }

                return stack;
            }
        }
        return stack;
    }

}
