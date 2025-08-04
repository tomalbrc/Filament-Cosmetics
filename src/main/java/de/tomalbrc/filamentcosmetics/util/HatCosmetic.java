package de.tomalbrc.filamentcosmetics.util;

import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.database.DatabaseManager;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class HatCosmetic {
    public ItemStack cosmeticItemStack = ItemStack.EMPTY;
    private final ServerPlayer player;

    public HatCosmetic(ServerPlayer player){
        this.player = player;
    }

    public void initItemStack() {
        this.cosmeticItemStack = DatabaseManager.getCosmetic(player, ItemType.HAT);
    }

    public void equip(ItemStack cosmeticStack){
        this.cosmeticItemStack = cosmeticStack;
        DatabaseManager.setCosmetic(this.player, ItemType.HAT, cosmeticStack);

        ItemStack targetItemStack;
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            targetItemStack = player.getInventory().getArmor(3);
        } else {
            targetItemStack = cosmeticStack;
        }
        sendHeadCosmeticsPacket(targetItemStack);
    }

    public void tick(){
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            return;
        }
        sendHeadCosmeticsPacket(cosmeticItemStack);
    }

    public void sendHeadCosmeticsPacket(ItemStack targetItemStack){
        this.player.connection.send(new ClientboundContainerSetSlotPacket(
                this.player.inventoryMenu.containerId,
                this.player.inventoryMenu.incrementStateId(),
                5, // Head Slot
                targetItemStack
        ));
    }
}
