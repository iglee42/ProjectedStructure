package fr.iglee42.projectedstructure.common.blocks.entity;

import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GhostBlockEntity extends BlockEntity {
    private BlockState stockedBlock = Blocks.BEDROCK.defaultBlockState();
    private int dispearTime = -1;

    public GhostBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModContent.GHOST_BLOCK_ENTITY.get(), p_155229_, p_155230_);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("stockedBlock", NbtUtils.writeBlockState(stockedBlock));
        tag.putInt("dispearTime",dispearTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.stockedBlock = NbtUtils.readBlockState(tag.getCompound("stockedBlock"));
        this.dispearTime = tag.getInt("dispearTime");
    }
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }


    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
       this.stockedBlock = NbtUtils.readBlockState(pkt.getTag().getCompound("stockedBlock"));
       this.dispearTime = pkt.getTag().getInt("dispearTime");
    }
    @NotNull
    @Override
    public IModelData getModelData() {
        if (this.remove) super.getModelData();
        ModelDataMap.Builder builder = new ModelDataMap.Builder()
                .withInitial(ProjectedStructure.PS_BLOCKSTATE, stockedBlock);
        return builder.build();
    }

    public void tick(Level level,BlockPos pos,BlockState state){
        if (dispearTime > 0){
            dispearTime--;
        }
        if (dispearTime == 0){
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        }
    }

    public BlockState getStockedBlock() {
        return stockedBlock;
    }

    public void setStockedBlock(BlockState stockedBlock) {
        this.stockedBlock = stockedBlock;
    }


    public void setDispearTime(int time) {
        dispearTime = time;
    }
}
