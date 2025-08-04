package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public class ArmorHeadSlotMixin {
    @Final
    @Shadow
    ServerPlayer field_29182;
    @ModifyVariable(
            method = "updateSlot",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifyHeadSlotItem(ItemStack stack, AbstractContainerMenu handler, int slot) {
        if(slot == 5) {
            ((ICosmetics) field_29182).getHatCosmetic().tick();
        }
        return stack;
    }
    @Inject(
            method = "updateState",
            at = @At(
                    value = "TAIL"
            )
    )
    void modifyHeadSlotItem (AbstractContainerMenu handler, NonNullList<ItemStack> stacks, ItemStack cursorStack, int[] properties, CallbackInfo ci) {
        if(handler instanceof InventoryMenu) {
            ((ICosmetics) field_29182).getHatCosmetic().tick();
        }
    }
}