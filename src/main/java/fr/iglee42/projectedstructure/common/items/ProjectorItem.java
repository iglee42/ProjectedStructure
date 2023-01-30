package fr.iglee42.projectedstructure.common.items;

import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.blocks.entity.GhostBlockEntity;
import fr.iglee42.projectedstructure.common.blocks.entity.ProjectorBlockEntity;
import fr.iglee42.projectedstructure.common.menu.ProjectorMenu;
import fr.iglee42.projectedstructure.common.network.ModMessages;
import fr.iglee42.projectedstructure.common.network.packets.ProjectorRotateC2S;
import fr.iglee42.projectedstructure.common.utils.ConfigStructures;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ProjectorItem extends Item {
    public ProjectorItem() {
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        if (!player.isCrouching()) {
            if (!stack.getOrCreateTag().contains("structureName")) {
                ServerPlayer sp = (ServerPlayer) player;
                MinecraftServer server = sp.getServer();
                List<String> structures = ConfigStructures.getStructures();
                NetworkHooks.openGui(sp, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return new TextComponent("Projector");
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                        return new ProjectorMenu(id, inv, stack, structures);
                    }
                });
            } else {
                if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
                    HitResult result = Minecraft.getInstance().hitResult;
                    Vec3 loc = level.getBlockState(new BlockPos(result.getLocation())).isAir() ? result.getLocation().subtract(0, 1, 0) : result.getLocation();
                    BlockPos pos = new BlockPos(loc.x , loc.y + 1,loc.z);
                    StructureManager structureManager = player.getServer().getStructureManager();
                    StructureTemplate s = null;
                    try {
                        InputStream stream = new FileInputStream(ConfigStructures.getStructure(stack.getOrCreateTag().getString("structurePath")));
                        s = structureManager.readStructure(NbtIo.readCompressed(stream));
                        stream.close();
                    } catch (Exception ignored) {}
                    CompoundTag pST = stack.getOrCreateTag().getCompound("structurePlacementSettings");
                    StructurePlaceSettings settings = new StructurePlaceSettings()
                            .setRotation(pST != null && pST.contains("rotation") ? Rotation.valueOf(pST.getString("rotation")) : Rotation.NONE);
                    if (level.getBlockState(pos.offset(0,-1,0)).is(ModContent.GHOST_BLOCK.get())) pos = pos.offset(0,-1,0);
                    if (s != null) {
                        StructureTemplate.processBlockInfos(level,pos,pos,settings,settings.getRandomPalette(s.palettes, pos).blocks(),s).forEach(sbi -> {
                            if (!sbi.state.isAir()) {
                                if (level.getBlockState(sbi.pos).is(Blocks.AIR)) {
                                    level.setBlockAndUpdate(sbi.pos, ModContent.GHOST_BLOCK.get().defaultBlockState());
                                    level.getBlockEntity(sbi.pos, ModContent.GHOST_BLOCK_ENTITY.get()).ifPresent(g -> {
                                        ((GhostBlockEntity) g).setStockedBlock(sbi.state);
                                        ((GhostBlockEntity) g).setDispearTime(30);
                                    });
                                }
                            }
                        });
                    }
                    BlockPos projectorPos = pos;
                    if (!level.getBlockState(pos.offset(1,0,0)).is(ModContent.GHOST_BLOCK.get())) {
                        level.destroyBlock(pos.offset(1, 0, 0), true);
                        projectorPos = pos.offset(1,0,0);
                    }
                    else if (!level.getBlockState(pos.offset(-1,0,0)).is(ModContent.GHOST_BLOCK.get())) {
                        level.destroyBlock(pos.offset(-1, 0, 0), true);
                        projectorPos = pos.offset(-1,0,0);
                    }
                    else if (!level.getBlockState(pos.offset(0,0,1)).is(ModContent.GHOST_BLOCK.get())) {
                        level.destroyBlock(pos.offset(0, 0, 1), true);
                        projectorPos = pos.offset(0,0,1);
                    }
                    else if (!level.getBlockState(pos.offset(0,0,-1)).is(ModContent.GHOST_BLOCK.get())) {
                        level.destroyBlock(pos.offset(0, 0, -1), true);
                        projectorPos = pos.offset(0,0,-1);
                    } else {
                        level.destroyBlock(pos.offset(0,1,0),true);
                        projectorPos = pos.offset(0,1,0);
                    }
                    level.setBlockAndUpdate(projectorPos,ModContent.PROJECTOR_BLOCK.get().defaultBlockState());
                    BlockPos finalPos = pos;
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                    level.getBlockEntity(projectorPos,ModContent.PROJECTOR_BLOCK_ENTITY.get()).ifPresent(be->{
                        ((ProjectorBlockEntity)be).setStructurePath(stack.getOrCreateTag().getString("structurePath"));
                        ((ProjectorBlockEntity)be).setBasePos(finalPos);
                        ((ProjectorBlockEntity)be).setRotation(settings.getRotation());
                        ((ProjectorBlockEntity)be).placeGhostBlocks();
                    });
                }
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;
        if (selected && stack.getOrCreateTag().contains("structureName") && entity instanceof Player player) {
            if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
                HitResult result = Minecraft.getInstance().hitResult;
                Vec3 loc = level.getBlockState(new BlockPos(result.getLocation())).isAir() ? result.getLocation().subtract(0, 1, 0) : result.getLocation();
                StructureManager structureManager = player.getServer().getStructureManager();
                StructureTemplate s = null;
                try {
                    InputStream stream = new FileInputStream(ConfigStructures.getStructure(stack.getOrCreateTag().getString("structurePath")));
                    s = structureManager.readStructure(NbtIo.readCompressed(stream));
                    stream.close();
                } catch (Exception ignored) {}
                CompoundTag pST = stack.getOrCreateTag().getCompound("structurePlacementSettings");
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setRotation(pST != null && pST.contains("rotation") ? Rotation.valueOf(pST.getString("rotation")) : Rotation.NONE);
                BlockPos pos = new BlockPos(loc.x , loc.y + 1,loc.z);
                if (level.getBlockState(pos.offset(0,-1,0)).is(ModContent.GHOST_BLOCK.get())) pos = pos.offset(0,-1,0);
                if (s != null) {
                    StructureTemplate.processBlockInfos(level,pos,pos,settings,settings.getRandomPalette(s.palettes, pos).blocks(),s).forEach(sbi -> {
                        if (!sbi.state.isAir()) {
                            if (level.getBlockState(sbi.pos).is(Blocks.AIR)) {
                                level.setBlockAndUpdate(sbi.pos, ModContent.GHOST_BLOCK.get().defaultBlockState());
                                level.getBlockEntity(sbi.pos, ModContent.GHOST_BLOCK_ENTITY.get()).ifPresent(g -> {
                                    ((GhostBlockEntity) g).setStockedBlock(sbi.state);
                                    ((GhostBlockEntity) g).setDispearTime(50);
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = ProjectedStructure.MODID, value = Dist.CLIENT)
    public static class ClientInputHandler {
        @SubscribeEvent
        public static void handleScroll(InputEvent.MouseScrollEvent event) {
            double delta = event.getScrollDelta();
            if (Minecraft.getInstance().player.getMainHandItem().is(ModContent.PROJECTOR.get()) && Minecraft.getInstance().player.isCrouching()){
                if (delta > 0){
                    ModMessages.sendToServer(new ProjectorRotateC2S(true,Minecraft.getInstance().player.getUUID()));
                } else if (delta < 0){
                    ModMessages.sendToServer(new ProjectorRotateC2S(false,Minecraft.getInstance().player.getUUID()));
                }
                event.setCanceled(true);
            }
        }
    }
}