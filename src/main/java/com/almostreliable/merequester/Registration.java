package com.almostreliable.merequester;

import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.capabilities.AppEngCapabilities;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.concurrent.atomic.AtomicReference;

import static com.almostreliable.merequester.MERequester.REQUESTER_ID;
import static com.almostreliable.merequester.MERequester.TERMINAL_ID;

public final class Registration {

    public static final BlockDefinition<RequesterBlock> REQUESTER_BLOCK = registerRequester();
    public static final ItemDefinition<PartItem<RequesterTerminalPart>> REQUESTER_TERMINAL = registerRequesterTerminal();
    private static final BlockEntityType<RequesterBlockEntity> REQUESTER_ENTITY = registerRequesterEntity();

    private Registration() {}

    private static BlockDefinition<RequesterBlock> registerRequester() {
        RequesterBlock block = new RequesterBlock();
        AEBaseBlockItem item = new AEBaseBlockItem(block, new Item.Properties());
        return new BlockDefinition<>("", Utils.getRL(REQUESTER_ID), block, item);
    }

    private static ItemDefinition<PartItem<RequesterTerminalPart>> registerRequesterTerminal() {
        PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminalPart.class));

        PartItem<RequesterTerminalPart> item = new PartItem<>(
            new Item.Properties(),
            RequesterTerminalPart.class,
            RequesterTerminalPart::new
        );
        return new ItemDefinition<>("", Utils.getRL(TERMINAL_ID), item);
    }

    private static BlockEntityType<RequesterBlockEntity> registerRequesterEntity() {
        AtomicReference<BlockEntityType<RequesterBlockEntity>> typeHolder = new AtomicReference<>();
        BlockEntityType.BlockEntitySupplier<RequesterBlockEntity> supplier = (blockPos, blockState)
            -> new RequesterBlockEntity(typeHolder.get(), blockPos, blockState);

        @SuppressWarnings("ConstantConditions")
        BlockEntityType<RequesterBlockEntity> type = BlockEntityType.Builder.of(supplier, REQUESTER_BLOCK.block()).build(null);
        typeHolder.set(type);

        AEBaseBlockEntity.registerBlockEntityItem(type, REQUESTER_BLOCK.asItem());
        REQUESTER_BLOCK.block().setBlockEntity(RequesterBlockEntity.class, type, null, null);

        return type;
    }

    static void registerContents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
            Tab.registerTab(event);
            return;
        }

        if (event.getRegistryKey() == Registries.BLOCK) {
            Registry.register(BuiltInRegistries.BLOCK, REQUESTER_BLOCK.id(), REQUESTER_BLOCK.block());
            Registry.register(BuiltInRegistries.ITEM, REQUESTER_BLOCK.id(), REQUESTER_BLOCK.asItem());
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, REQUESTER_BLOCK.id(), REQUESTER_ENTITY);
            Registry.register(BuiltInRegistries.ITEM, REQUESTER_TERMINAL.id(), REQUESTER_TERMINAL.asItem());
        }
    }

    static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AppEngCapabilities.IN_WORLD_GRID_NODE_HOST, REQUESTER_ENTITY, (requester, ctx) -> requester);
    }

    public static final class Tab {

        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Utils.getRL("tab"));
        private static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(Utils.translate("itemGroup", "tab"))
            .icon(REQUESTER_BLOCK::stack)
            .noScrollBar()
            .build();

        private Tab() {}

        static void initContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == TAB_KEY) {
                event.accept(REQUESTER_BLOCK);
                event.accept(REQUESTER_TERMINAL);
            }
        }

        private static void registerTab(RegisterEvent registerEvent) {
            registerEvent.register(Registries.CREATIVE_MODE_TAB, TAB_KEY.location(), () -> TAB);
        }
    }
}
