package fr.iglee42.projectedstructure.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.iglee42.projectedstructure.common.blocks.entity.GhostBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GhostBlockRenderer implements BlockEntityRenderer<GhostBlockEntity> {
    public GhostBlockRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(GhostBlockEntity gbe, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        if (gbe.getStockedBlock().hasBlockEntity() && gbe.getStockedBlock().getBlock() instanceof BaseEntityBlock beb){
            BlockEntity be = beb.newBlockEntity(gbe.getBlockPos(),gbe.getStockedBlock());
            Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be).render(be,p_112308_,p_112309_,p_112310_,p_112311_, OverlayTexture.RED_OVERLAY_V);
        }
    }
}
