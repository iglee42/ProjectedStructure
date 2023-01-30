package fr.iglee42.projectedstructure.common.blocks;

import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.blocks.entity.GhostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GhostBlock extends BaseEntityBlock {
    public GhostBlock() {
        super(Properties.of(Material.GLASS).strength(-1,36000).noOcclusion().noCollission());
    }

    public boolean propagatesSkylightDown(BlockState p_49100_, BlockGetter p_49101_, BlockPos p_49102_) {
        return true;
    }

    public float getShadeBrightness(BlockState p_49094_, BlockGetter p_49095_, BlockPos p_49096_) {
        return 1.0F;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GhostBlockEntity(pos,state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lvl, BlockState state, BlockEntityType<T> type) {
        return (level,blockPos,blockState,be) -> {if (type == ModContent.GHOST_BLOCK_ENTITY.get()) ((GhostBlockEntity)be).tick(level,blockPos,blockState);};
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext placeContext) {
        boolean flag = false;
        if (placeContext.getLevel().getBlockEntity(placeContext.getClickedPos()) instanceof GhostBlockEntity be && !state.getShape(placeContext.getLevel(),placeContext.getClickedPos(),CollisionContext.of(placeContext.getPlayer())).isEmpty())
            flag = be.getStockedBlock().getBlock().asItem() == placeContext.getItemInHand().getItem();
        return flag;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        GhostBlockEntity be = ((GhostBlockEntity)level.getBlockEntity(pos));
        return !state.getShape(level,pos,CollisionContext.of(player)).isEmpty() ? be.getStockedBlock().getCloneItemStack(target,level,pos,player) : null;
    }

    @Override
    public VoxelShape getShape(BlockState p_60479_, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return context.isHoldingItem(((GhostBlockEntity)getter.getBlockEntity(pos)).getStockedBlock().getBlock().asItem()) ? Shapes.block() : Shapes.box(0.4,0.4,0.4,0.6,0.6,0.6);
    }


}
