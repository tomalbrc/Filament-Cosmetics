package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$2")
public class ServerPlayerEntityMixin {
    @Final
    @Shadow
    ServerPlayer field_29183;
    @Inject(
            method = "onSlotUpdate",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem (AbstractContainerMenu handler, int slot, ItemStack _stack, CallbackInfo ci) {
        if(handler instanceof InventoryMenu) {
            if(slot == 5) {
                ((ICosmetics) field_29183).getHatCosmetic().tick();
            }
        }
    }
}
