package fr.iglee42.projectedstructure.common.blocks;

import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.blocks.entity.ProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProjectorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ProjectorBlock() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL).noOcclusion().noCollission().strength(2,36000));
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level level, BlockPos p_60505_, Player player, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if (level.isClientSide()) return InteractionResult.sidedSuccess(level.isClientSide());
        if (player.isCrouching()) {
            if (level.getBlockEntity(p_60505_) instanceof ProjectorBlockEntity be){
                be.placeGhostBlocks();
            }
        }
        return super.use(p_60503_, level, p_60505_, player, p_60507_, p_60508_);
    }

    public boolean propagatesSkylightDown(BlockState p_49100_, BlockGetter p_49101_, BlockPos p_49102_) {
        return true;
    }

    public float getShadeBrightness(BlockState p_49094_, BlockGetter p_49095_, BlockPos p_49096_) {
        return 1.0F;
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape,Shapes.box(0.155, 0d, 0d,0.845, 0.315d, 0.435d), BooleanOp.OR);
        shape = Shapes.join(shape,Shapes.box(0.155, 0.315d, 0d,0.845, 0.375d, 0.475d), BooleanOp.OR);
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ProjectorBlockEntity(p_153215_,p_153216_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lvl, BlockState blockState, BlockEntityType<T> type) {
        return (level,pos,state,be) ->{
            if (be instanceof ProjectorBlockEntity pb)pb.tick(level,pos,state);
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootContext.Builder p_60538_) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModContent.PROJECTOR.get()));
        return drops;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState1 , boolean p_60519_) {
        if (level.getBlockEntity(blockPos) instanceof ProjectorBlockEntity be)be.removeGhostBlocks();
        super.onRemove(blockState, level, blockPos, blockState1, p_60519_);
    }
}
