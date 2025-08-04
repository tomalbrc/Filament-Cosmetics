package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.database.DatabaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
            method = "method_30120",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;",
                    ordinal = 0
            )
    )
    ItemStack modifyHeadSlotItem (ItemStack instance, List list, EquipmentSlot slot, ItemStack stack) {
        if ((LivingEntity) (Object) this instanceof ServerPlayer player){
            if(slot.getIndex() == 3) {
                ItemStack cosmeticsIS = DatabaseManager.getCosmetic(player, ItemType.HAT);;
                if (cosmeticsIS != ItemStack.EMPTY) {
                    return cosmeticsIS;
                }
            }
        }
        return instance.copy();
    }
}
