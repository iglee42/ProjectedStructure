package fr.iglee42.projectedstructure.common.menu;

import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.network.ModMessages;
import fr.iglee42.projectedstructure.common.network.packets.ProjectorNewItemC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ProjectorMenu extends AbstractContainerMenu {

    private final Inventory playerInventory;
    private final ItemStack projector;

    private List<String> structures;

    public int tick = 0;

    public ProjectorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv,new ItemStack(ModContent.PROJECTOR.get()),new ArrayList<>());
    }
    public ProjectorMenu(int id, Inventory inv, ItemStack projector,List<String> structures) {
        super(ModContent.PROJECTOR_MENU.get(), id);
        this.playerInventory = inv;
        this.projector = projector;
        this.structures = structures;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

    public List<String> getStructures() {
        return structures;
    }

    public void setStructures(List<String> structures) {
        this.structures = structures;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public ItemStack getProjector() {
        return projector;
    }

    public void giveNewProjector(String structureName,String path) {
        /*ItemStack newStack = getProjector().copy();
        CompoundTag newTag = newStack.getOrCreateTag();
        newTag.putString("structureName",structureName);
        newTag.putString("structurePath",path);
        newStack.setTag(newTag);
        getPlayerInventory().setItem(getPlayerInventory().selected,ItemStack.EMPTY);
        ItemEntity entity = new ItemEntity(getPlayerInventory().player.level,getPlayerInventory().player.getX(),getPlayerInventory().player.getY(),getPlayerInventory().player.getZ(),newStack);
        entity.setNoPickUpDelay();
        entity.setOwner(getPlayerInventory().player.getUUID());
        System.out.println(entity.getItem().getOrCreateTag().toString());
        getPlayerInventory().player.level.addFreshEntity(entity);*/
        ModMessages.sendToServer(new ProjectorNewItemC2SPacket(structureName,path,getPlayerInventory().player.getUUID()));
        Minecraft.getInstance().player.closeContainer();
    }
}
