package fr.iglee42.projectedstructure.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class ProjectorClearStructureC2S {

    private final UUID playerUUID;

    public ProjectorClearStructureC2S(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public ProjectorClearStructureC2S(FriendlyByteBuf buf) {
        playerUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID).getMainHandItem().getOrCreateTag().remove("structureName");
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID).getMainHandItem().getOrCreateTag().remove("structurePath");
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID).getMainHandItem().getOrCreateTag().remove("structurePlacementSettings");
        });
        return true;
    }
}
