package fr.iglee42.projectedstructure.common.blocks.entity;

import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.utils.ConfigStructures;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProjectorBlockEntity extends BlockEntity {

    private BlockPos basePos = new BlockPos(0,0,0);
    private String structurePath = "";
    private Rotation rotation = Rotation.NONE;

    public static void setBasePosition(Level level,BlockPos entityPos,BlockPos basePos){
        if (level.getBlockEntity(entityPos) instanceof ProjectorBlockEntity be)be.setBasePos(basePos);
    }

    public ProjectorBlockEntity( BlockPos p_155229_, BlockState p_155230_) {
        super(ModContent.PROJECTOR_BLOCK_ENTITY.get(), p_155229_, p_155230_);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("basePos",NbtUtils.writeBlockPos(basePos));
        if (!structurePath.isEmpty()) tag.putString("structure", structurePath);
        tag.putString("rotation",rotation.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        basePos = NbtUtils.readBlockPos(tag.getCompound("basePos"));
        if (tag.contains("structure")) structurePath = tag.getString("structure");
        rotation = Rotation.valueOf(tag.getString("rotation"));
    }

    public void setBasePos(BlockPos basePos) {
        this.basePos = basePos;
    }

    public void setStructurePath(String structurePath) {
        this.structurePath = structurePath;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public void placeGhostBlocks(){
        if (this.level.isClientSide()) return;
        StructureManager structureManager = this.level.getServer().getStructureManager();
        Optional<StructureTemplate> structure = Optional.empty();
        try {
            InputStream stream = new FileInputStream(ConfigStructures.getStructure(structurePath));
            structure = Optional.of(structureManager.readStructure(NbtIo.readCompressed(stream)));
            stream.close();
        } catch (Exception ignored) {}
        AtomicBoolean hasAllBlocks = new AtomicBoolean(true);
        structure.ifPresent(s->{
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
            StructureTemplate.processBlockInfos(level,basePos,basePos,settings,settings.getRandomPalette(s.palettes, basePos).blocks(),s).forEach(sbi -> {
                if (!sbi.state.isAir()) {
                    if (level.getBlockState(sbi.pos).is(Blocks.AIR)) {
                        level.setBlockAndUpdate(sbi.pos, ModContent.GHOST_BLOCK.get().defaultBlockState());
                        level.getBlockEntity(sbi.pos, ModContent.GHOST_BLOCK_ENTITY.get()).ifPresent(g -> {
                            g.setStockedBlock(sbi.state);
                            g.setDispearTime(-1);
                        });
                        hasAllBlocks.set(false);
                    } else if (level.getBlockEntity(sbi.pos) instanceof GhostBlockEntity){
                        hasAllBlocks.set(false);
                        if (level.getBlockState(sbi.pos.offset(0,1,0)).is(ModContent.WARNING_BLOCK.get())){
                            level.setBlockAndUpdate(sbi.pos.offset(0,1,0),Blocks.AIR.defaultBlockState());
                        }
                    } else if (!level.getBlockState(sbi.pos).is(ModContent.PROJECTOR_BLOCK.get())){
                        if (!level.getBlockState(sbi.pos).is(sbi.state.getBlock())) {
                            hasAllBlocks.set(false);
                            if (level.getBlockState(sbi.pos.offset(0,1,0)).is(Blocks.AIR)){
                                level.setBlockAndUpdate(sbi.pos.offset(0,1,0),ModContent.WARNING_BLOCK.get().defaultBlockState());
                            }
                        } else if (level.getBlockState(sbi.pos.offset(0,1,0)).is(ModContent.WARNING_BLOCK.get())){
                            level.setBlockAndUpdate(sbi.pos.offset(0,1,0),Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            });
            //s.placeInWorld((ServerLevelAccessor) this.level,basePos,basePos,settings,random,2);
        });
        if (hasAllBlocks.get()){
            level.destroyBlock(this.getBlockPos(),true);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        placeGhostBlocks();
    }

    public void removeGhostBlocks() {
        if (this.level.isClientSide()) return;
        StructureManager structureManager = this.level.getServer().getStructureManager();
        Optional<StructureTemplate> structure = Optional.empty();
        try {
            InputStream stream = new FileInputStream(ConfigStructures.getStructure(structurePath));
            structure = Optional.of(structureManager.readStructure(NbtIo.readCompressed(stream)));
            stream.close();
        } catch (Exception ignored) {}
        structure.ifPresent(s->{
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
            StructureTemplate.processBlockInfos(level,basePos,basePos,settings,settings.getRandomPalette(s.palettes, basePos).blocks(),s).forEach(sbi -> {
                if (!sbi.state.isAir()) {
                    if (level.getBlockEntity(sbi.pos) instanceof GhostBlockEntity gbe){
                        level.setBlockAndUpdate(sbi.pos,Blocks.AIR.defaultBlockState());
                    } else if (level.getBlockState(sbi.pos.offset(0,1,0)).is(ModContent.WARNING_BLOCK.get())){
                        level.setBlockAndUpdate(sbi.pos.offset(0,1,0),Blocks.AIR.defaultBlockState());
                    }
                }
            });
        });
    }
}
