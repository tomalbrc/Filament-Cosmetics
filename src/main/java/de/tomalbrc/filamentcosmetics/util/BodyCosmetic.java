package de.tomalbrc.filamentcosmetics.util;

import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.database.DatabaseManager;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import static de.tomalbrc.filamentcosmetics.database.DatabaseManager.setCosmetic;

public class BodyCosmetic {

    private final Display.ItemDisplay bodyCosmetics;
    private ItemStack cosmeticItemStack = ItemStack.EMPTY;
    private final ServerPlayer player;

    public BodyCosmetic(ServerPlayer player){
        this.bodyCosmetics = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, player.serverLevel());
        this.player = player;
    }

    public void equip(ItemStack is) {
        setCosmetic(player, ItemType.BODY_COSMETIC, is);
        this.cosmeticItemStack = is;
        initNewCosmetic();
    }

    public void initNewCosmetic() {
        this.cosmeticItemStack = DatabaseManager.getCosmetic(player, ItemType.BODY_COSMETIC);
        bodyCosmetics.setPos(player.getX(), player.getY(), player.getZ());

        bodyCosmetics.setItemStack(cosmeticItemStack);
        bodyCosmetics.setInvulnerable(true);
        bodyCosmetics.setNoGravity(true);

        player.serverLevel().getChunkSource().broadcastAndSend(player,
                new ClientboundAddEntityPacket(bodyCosmetics, 1, bodyCosmetics.blockPosition()));

        player.serverLevel().getChunkSource().broadcastAndSend(player,
                new ClientboundSetEntityDataPacket(bodyCosmetics.getId(),
                        bodyCosmetics.getEntityData().getNonDefaultValues()));

        bodyCosmetics.startRiding(player);
    }

    public void tick() {
        // We still position the cosmetic at the player's location.
        // The Y-offset might need adjustment based on the pose (e.g., sneaking).
        double yOffset = player.isShiftKeyDown() ? 1.55 : 1.8;
        bodyCosmetics.setPos(player.getX(), player.getY() + yOffset, player.getZ());

        // --- YAW PREDICTION ---
        // This is the core logic for replicating the client's rendered body yaw.

        float yawToUse;
        // Get horizontal velocity squared. Using squared values avoids a square root calculation.
        double velX = player.getDeltaMovement().x();
        double velZ = player.getDeltaMovement().z();
        double horizontalVelocitySq = velX * velX + velZ * velZ;

        // Check if the player is moving horizontally.
        // The threshold (1.0E-6) is small to detect any meaningful movement but ignore tiny jitters.
        if (horizontalVelocitySq > 1.0E-6) {
            // If moving, the client renders the body facing the same direction as the head.
            // So, we use the player's head yaw.
            yawToUse = player.getYRot();
        } else {
            // If not moving, the client uses the standard body yaw logic (lagging behind the head).
            // The server's getBodyYaw() is perfect for this.
            yawToUse = player.getVisualRotationYInDegrees();
        }


        // --- PITCH PREDICTION ---
        // We need to handle several player states for accurate vertical rotation.

        float pitchToUse;
        if (player.isSwimming() || player.isVisuallyCrawling()) {
            // When swimming or crawling, the player's body is horizontal.
            pitchToUse = 90.0F;
        } else if (player.isShiftKeyDown()) {
            // Your original logic for sneaking pitch is good.
            pitchToUse = 28.0F;
        } else {
            // When standing/walking, the body doesn't pitch with the head.
            // A pitch of 0.0F keeps the cosmetic upright.
            // If you want a subtle tilt when the player looks up/down, you can use:
            // pitchToUse = player.getPitch() * 0.4f; // Sclae it down to avoid extreme tilting
            pitchToUse = 0.0F;
        }


        // --- SENDING THE PACKET ---
        // Now we send the update packet with our predicted yaw and pitch.
        player.serverLevel().getChunkSource().broadcastAndSend(player,
                new ClientboundMoveEntityPacket.Rot(
                        bodyCosmetics.getId(),
                        (byte) Mth.floor(yawToUse * 256.0F / 360.0F),
                        (byte) Mth.floor(pitchToUse * 256.0F / 360.0F),
                        false // onGround status doesn't matter much for a DisplayEntity
                )
        );
    }
}