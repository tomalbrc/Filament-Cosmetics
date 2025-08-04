package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayer player;

    @Inject(
            method = "onPickFromInventory",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem2 (ServerboundPickItemPacket packet, CallbackInfo ci) {
        ((ICosmetics) player).getHatCosmetic().tick();
    }

    @Inject(
            method = "onClickSlot",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem3 (ServerboundContainerClickPacket packet, CallbackInfo ci) {
        AbstractContainerMenu handler = this.player.containerMenu;
        if(handler instanceof InventoryMenu) {
            if(packet.getSlotNum() == 5) {
                ((ICosmetics) player).getHatCosmetic().tick();
            }
        }
    }
}