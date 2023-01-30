package fr.iglee42.projectedstructure.common.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class ProjectorRotateC2S {

    private boolean forward;
    private UUID playerUUID;

    public ProjectorRotateC2S(boolean forward, UUID playerUUID) {
        this.forward = forward;
        this.playerUUID = playerUUID;
    }

    public ProjectorRotateC2S(FriendlyByteBuf buf){
        this.forward = buf.readBoolean();
        this.playerUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(forward);
        buf.writeUUID(playerUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
            ItemStack stack = player.getMainHandItem();
            boolean hasStockedRotation = stack.getOrCreateTag().contains("structurePlacementSettings") && stack.getOrCreateTag().getCompound("structurePlacementSettings").contains("rotation");
            Rotation currentRotation = hasStockedRotation ? Rotation.valueOf(stack.getOrCreateTag().getCompound("structurePlacementSettings").getString("rotation")) : Rotation.NONE;
            switch (currentRotation){
                case NONE -> currentRotation = forward ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
                case CLOCKWISE_90 -> currentRotation = forward ? Rotation.CLOCKWISE_180 : Rotation.NONE;
                case CLOCKWISE_180 -> currentRotation = forward ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
                case COUNTERCLOCKWISE_90 -> currentRotation = forward ? Rotation.NONE : Rotation.CLOCKWISE_180;
            }
            if (stack.getOrCreateTag().contains("structurePlacementSettings")){
                stack.getOrCreateTag().getCompound("structurePlacementSettings").putString("rotation",currentRotation.name());
            } else {
                CompoundTag settings = new CompoundTag();
                settings.putString("rotation",currentRotation.name());
                stack.getOrCreateTag().put("structurePlacementSettings",settings);
            }
        });
        return true;
    }
}
