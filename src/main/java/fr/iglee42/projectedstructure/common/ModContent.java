package fr.iglee42.projectedstructure.common;

import fr.iglee42.projectedstructure.ProjectedStructure;
import fr.iglee42.projectedstructure.common.blocks.GhostBlock;
import fr.iglee42.projectedstructure.common.blocks.ProjectorBlock;
import fr.iglee42.projectedstructure.common.blocks.entity.GhostBlockEntity;
import fr.iglee42.projectedstructure.common.blocks.entity.ProjectorBlockEntity;
import fr.iglee42.projectedstructure.common.menu.ProjectorMenu;
import fr.iglee42.projectedstructure.common.items.ProjectorItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContent {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ProjectedStructure.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,ProjectedStructure.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES,ProjectedStructure.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS,ProjectedStructure.MODID);

    public static final RegistryObject<Item> PROJECTOR = ITEMS.register("projector", ProjectorItem::new);
    public static final RegistryObject<Block> PROJECTOR_BLOCK = BLOCKS.register("projector", ProjectorBlock::new);
    public static final RegistryObject<BlockEntityType<?>> PROJECTOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("projector_block",()->BlockEntityType.Builder.of(ProjectorBlockEntity::new,ModContent.PROJECTOR_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<ProjectorMenu>> PROJECTOR_MENU = MENUS.register("projector_menu",()-> IForgeMenuType.create(ProjectorMenu::new));

    public static final RegistryObject<Block> GHOST_BLOCK = BLOCKS.register("ghost_block", GhostBlock::new);
    public static final RegistryObject<BlockEntityType<GhostBlockEntity>> GHOST_BLOCK_ENTITY = BLOCK_ENTITIES.register("ghost_block",()->BlockEntityType.Builder.of(GhostBlockEntity::new,ModContent.GHOST_BLOCK.get()).build(null));

}
