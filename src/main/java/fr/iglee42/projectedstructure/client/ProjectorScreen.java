package fr.iglee42.projectedstructure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.menu.ProjectorMenu;
import fr.iglee42.projectedstructure.common.utils.ConfigStructures;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectorScreen extends AbstractContainerScreen<ProjectorMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(ProjectedStructure.MODID,"textures/gui/projector_gui.png");
    private static final int MAX_PAGE_SIZE = 5;

    private int imgWidth = (int) (imageWidth * 1.45);
    private int imgHeight = (int) (imageHeight * 1.45);

    private int x;
    private int y;

    private int centerX;
    private int centerY;

    private int lastWidth;
    private int lastHeight;

    private StructureButton selectedButton = null;


    private final List<List<StructureButton>> pages = new ArrayList<>();
    private int currentPage = 0;

    private Button previousPageButton;
    private Button nextPageButton;

    public ProjectorScreen(ProjectorMenu menu, Inventory inv,Component ignored) {
        super(menu, inv, new TextComponent(""));
        this.playerInventoryTitle = new TextComponent("");
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - this.imgWidth) / 2;
        y = (this.height - this.imgHeight) / 2;
        //ModMessages.sendToServer(new ProjectorStructuresSyncC2SPacket());
        menu.setStructures(ConfigStructures.getStructures());
        Collections.reverse(menu.getStructures());
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float light, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        this.blit(poseStack, x, y + 15, 0, 0, this.imgWidth, this.imgHeight);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float light) {
        this.renderBackground(poseStack);
        if (lastWidth != width || lastHeight != height){
            lastWidth = width;
            lastHeight = height;
            centerX = width  / 2;
            centerY = height  / 2;
            pages.clear();
            renderables.clear();
            AtomicInteger currentPageIndex = new AtomicInteger(0);
            AtomicInteger pageIndex = new AtomicInteger(0);
            List<String> paths = ConfigStructures.getStructuresPaths();
            List<String> allStructures = new ArrayList<>();
            menu.getStructures().forEach(s->allStructures.add(s));
            menu.getStructures().forEach(r->{
                String path = paths.stream().filter(s->s.endsWith(r)).findFirst().orElse("Unknown Path");
                paths.remove(path);
                StructureButton b = new StructureButton(centerX - 15, (centerY - 75) + (currentPageIndex.get() * 20), 131, 20, new TextComponent(r), bt -> selectedButton = (StructureButton) bt,r,path);
                allStructures.remove(r);
                if (pages.size() < pageIndex.get() + 1)
                    pages.add(new ArrayList<>());
                pages.get(pageIndex.get()).add(b);
                if (currentPageIndex.incrementAndGet() == 5) {
                    currentPageIndex.set(0);
                    pageIndex.incrementAndGet();
                }
            });
            previousPageButton = addRenderableWidget(new Button(centerX + 40, centerY - 96, 20, 20, new TextComponent("▲"),btn->{
                if (currentPage - 1 >= 0) {
                    currentPage--;
                    renderables.clear();
                    selectedButton = null;
                }
            }));
            nextPageButton = addRenderableWidget(new Button(centerX + 40, centerY + 27, 20, 20, new TextComponent("▼"),btn->{
                if (currentPage + 1 < pages.size()) {
                    currentPage++;
                    renderables.clear();
                    selectedButton = null;
                }
            }));

        }
        if (currentPage >= pages.size()) currentPage = pages.size() - 1;
        this.pages.stream().filter(l->pages.indexOf(l) != currentPage).forEach(l->l.forEach(b->b.active = false));
        this.pages.get(currentPage).forEach(b->{
            if (selectedButton != b)b.active = true;
            if (!renderables.contains(b)) addRenderableWidget(b);
        });
        addRenderableWidget(previousPageButton);
        addRenderableWidget(nextPageButton);
        super.render(poseStack,mouseX,mouseY,light);
        this.renderTooltip(poseStack, mouseX, mouseY);
        menu.tick++;
        if (selectedButton != null){
            this.pages.forEach(l->l.stream().filter(b -> selectedButton.equals(b)).findFirst().ifPresent(b->b.active = false));
            renderStructureImage(selectedButton.getPath(),poseStack);
            drawCenteredString(poseStack,font,"Name : " + selectedButton.getStructureName(),centerX - 69, centerY - 10, ChatFormatting.WHITE.getColor());
            drawCenteredString(poseStack,font,"Path : ",centerX - 69, centerY , ChatFormatting.WHITE.getColor());
            drawCenteredString(poseStack,font,selectedButton.getPath().substring(1) +".nbt",centerX - 71, centerY + 10, ChatFormatting.WHITE.getColor());
            addRenderableWidget(new Button(centerX-110,centerY + 20,75,20,new TextComponent("Validate"),btn-> menu.giveNewProjector(selectedButton.getStructureName(),selectedButton.getPath())));
        } else {
            renderables.removeIf(w-> w instanceof Button btn && btn.getMessage().getString().equals("Validate"));
        }
        this.previousPageButton.active = currentPage > 0;
        this.nextPageButton.active = currentPage < pages.size() -1 ;
    }

    private void renderStructureImage(String path,PoseStack stack) {
        if (ConfigStructures.getImage(path) != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, new ResourceLocation(ProjectedStructure.MODID,path));
            RenderSystem.enableBlend();
            blit(stack, centerX - 110, centerY - 92, 0.0F, 0.0F, 80, 80, 80, 80);
            RenderSystem.disableBlend();
        } else {
            drawCenteredString(stack,font,"Missing image file : ",centerX - 69, centerY - 65 , ChatFormatting.WHITE.getColor());
            drawCenteredString(stack,font,selectedButton.getPath().substring(1) +".png",centerX - 71, centerY - 55, ChatFormatting.WHITE.getColor());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
