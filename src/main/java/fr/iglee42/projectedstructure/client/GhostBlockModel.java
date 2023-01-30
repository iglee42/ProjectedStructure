package fr.iglee42.projectedstructure.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.iglee42.projectedstructure.common.utils.ModelDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static fr.iglee42.projectedstructure.ProjectedStructure.PS_BLOCKSTATE;
import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

public class GhostBlockModel implements BakedModel {
    private static final Supplier<BlockRenderDispatcher> DISPATCHER = () -> Minecraft.getInstance().getBlockRenderer();

    private final BakedModel model;

    public GhostBlockModel(BakedModel model) {
        this.model = model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random random, IModelData extraData) {
        Optional<BlockState> data = ModelDataUtils.getData(extraData, PS_BLOCKSTATE);
        if (!data.isPresent()) {
            return Collections.emptyList();
        }
        BlockState mirrorState = data.get();
        Supplier<List<BakedQuad>> quads = () -> this.render(mirrorState, state, DISPATCHER.get().getBlockModel(state), side, random, extraData);
        if (MinecraftForgeClient.getRenderType() == RenderType.translucent()){
            List<BakedQuad> quadList = quads.get();
            return this.getOverlay(this.gatherAllQuads(quads));
        }
        return quads.get();
    }

    private List<BakedQuad> getOverlay(List<BakedQuad> allQuads){
        List<BakedQuad> quads = new ArrayList<>(allQuads);
        for (BakedQuad quad : allQuads){
            quads.add(generateOverlayQuad(quad));
        }
        return quads;
    }
    protected List<BakedQuad> render(@Nonnull BlockState mirrorState, @Nonnull BlockState baseState, @Nonnull BakedModel model, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return new ArrayList<>(model.getQuads(mirrorState, side, rand, extraData));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_119123_, @Nullable Direction p_119124_, Random p_119125_) {
        return this.model.getQuads(p_119123_, p_119124_, p_119125_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.model.useAmbientOcclusion();
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return this.model.useAmbientOcclusion(state);
    }

    @Override
    public boolean isGui3d() {
        return this.model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.model.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.model.getOverrides();
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return this.model.handlePerspective(cameraTransformType, poseStack);
    }
    public BakedQuad generateOverlayQuad(BakedQuad quad) {
        int[] data = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
        for (int i = 0; i < 4; i++) {
            int j = DefaultVertexFormat.BLOCK.getIntegerSize() * i;

            float x = intBitsToFloat(data[j]) + 0.001F*quad.getDirection().getStepX();
            float y = intBitsToFloat(data[j+1]) + 0.001F*quad.getDirection().getStepY();
            float z = intBitsToFloat(data[j+2]) + 0.001F*quad.getDirection().getStepZ();

            //data[j] = floatToRawIntBits(x);
            //data[j+1] = floatToRawIntBits(y);
            //data[j+2] = floatToRawIntBits(z);

            float ui;
            float vi;

            switch (quad.getDirection().getAxis()) {
                case X -> {
                    ui = z;
                    vi = 1 - y;
                }
                default -> {
                    ui = x;
                    vi = z;
                }
                case Z -> {
                    ui = x;
                    vi = 1 - y;
                }
            }

            data[j+4] = floatToRawIntBits(ClientEvents.ghostOverlaySprite.getU(ui*16F));
            data[j+5] = floatToRawIntBits(ClientEvents.ghostOverlaySprite.getV(vi*16F));

            //data[j+6] = (240 << 16) | 240;
        }

        return new BakedQuad(data, -1, quad.getDirection(), ClientEvents.ghostOverlaySprite, quad.isShade());

    }
    protected List<BakedQuad> gatherAllQuads(Supplier<List<BakedQuad>> superQuads) {
        RenderType layer = MinecraftForgeClient.getRenderType();

        ForgeHooksClient.setRenderType(null);
        List<BakedQuad> quads = new ArrayList<>(superQuads.get());
        ForgeHooksClient.setRenderType(layer);
        return quads;
    }
}
