package fr.iglee42.projectedstructure;

import com.mojang.logging.LogUtils;
import fr.iglee42.projectedstructure.client.ClientEvents;
import fr.iglee42.projectedstructure.client.ProjectorScreen;
import fr.iglee42.projectedstructure.common.ModContent;
import fr.iglee42.projectedstructure.common.network.ModMessages;
import fr.iglee42.projectedstructure.common.utils.ConfigStructures;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProjectedStructure.MODID)
public class ProjectedStructure {

    public static final String MODID = "projectedstructure";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ModelProperty<BlockState> PS_BLOCKSTATE = new ModelProperty<>();
    public static final ModelProperty<FluidState> PS_FLUIDSTATE = new ModelProperty<>();

    public ProjectedStructure() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContent.ITEMS.register(bus);
        ModContent.BLOCKS.register(bus);
        ModContent.BLOCK_ENTITIES.register(bus);
        ModContent.MENUS.register(bus);
        ModMessages.register();
        ConfigStructures.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(ClientEvents::onBlockColors);
            bus.addListener(ClientEvents::onModelBaked);
            bus.addListener(ClientEvents::onTextureStitch);
            bus.addListener(ClientEvents::onTextureStitched);
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModContent.PROJECTOR_MENU.get(), ProjectorScreen::new);
        }
    }


}
