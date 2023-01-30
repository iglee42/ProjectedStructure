package fr.iglee42.projectedstructure.common.network;

import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ProjectedStructure.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(ProjectorNewItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ProjectorNewItemC2SPacket::new)
                .encoder(ProjectorNewItemC2SPacket::toBytes)
                .consumer(ProjectorNewItemC2SPacket::handle)
                .add();
        net.messageBuilder(ProjectorRotateC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ProjectorRotateC2S::new)
                .encoder(ProjectorRotateC2S::toBytes)
                .consumer(ProjectorRotateC2S::handle)
                .add();

    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}