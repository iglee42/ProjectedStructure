package fr.iglee42.projectedstructure.common.network.packets;

import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.menu.ProjectorMenu;
import fr.iglee42.projectedstructure.common.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ProjectorNewItemC2SPacket {

    private String name,path;
    private UUID playerUUID;

    public ProjectorNewItemC2SPacket(String name, String path, UUID playerUUID) {
        this.name = name;
        this.path = path;
        this.playerUUID = playerUUID;
    }

    public ProjectorNewItemC2SPacket(FriendlyByteBuf buf) {
        int nameLength = buf.readInt();
        char[] nameChars = new char[nameLength];
        for (int i = 0; i < nameLength; i++){
            nameChars[i] = buf.readChar();
        }
        this.name = String.valueOf(nameChars);
        int pathLength = buf.readInt();
        char[] pathChars = new char[pathLength];
        for (int i = 0; i < pathLength; i++){
            pathChars[i] = buf.readChar();
        }
        this.path = String.valueOf(pathChars);
        this.playerUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(name.length());
        for (int i = 0;i < name.length();i++){
            buf.writeChar(name.charAt(i));
        }
        buf.writeInt(path.length());
        for (int i = 0; i < path.length(); i++){
            buf.writeChar(path.charAt(i));
        }
        buf.writeUUID(playerUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player.containerMenu instanceof ProjectorMenu){
                ItemStack stack = new ItemStack(ModContent.PROJECTOR.get());
                CompoundTag tag = new CompoundTag();
                tag.putString("structureName", name);
                tag.putString("structurePath", path);
                stack.setTag(tag);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            }
        });
        return true;
    }
}
