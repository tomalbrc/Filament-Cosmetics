package de.tomalbrc.filamentcosmetics.mixin;

import de.tomalbrc.filamentcosmetics.ext.ICosmetics;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(
            method = "onPlayerConnect",
            at = @At( value = "TAIL" )
    )
    void modifyHeadSlotItem(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        ((ICosmetics) player).getHatCosmetic().initItemStack();
        ((ICosmetics) player).getHatCosmetic().tick();
        ((ICosmetics) player).getBodyCosmetics().initNewCosmetic();
    }
}
