package com.almostreliable.merequester;

import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.almostreliable.merequester.mixin.AEBlockEntitiesAccessorMixin;
import com.almostreliable.merequester.mixin.AEBlocksInvokerMixin;
import com.almostreliable.merequester.mixin.AEItemsInvokerMixin;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import com.almostreliable.merequester.utils.Utils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegisterEvent;

import java.util.concurrent.atomic.AtomicReference;

public final class Registration {

    private Registration() {}

    private static final Tab TAB = new Tab();

    public static ItemDefinition<PartItem<RequesterTerminalPart>> setupTerminal() {
        PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminalPart.class));
        return AEItemsInvokerMixin.merequester$partItem(
            "",
            Utils.getRL(MERequester.TERMINAL_ID),
            props -> new PartItem<>(props, RequesterTerminalPart.class, RequesterTerminalPart::new),
            TAB
        );
    }

    // TODO: check why this does not add the block item to the creative tab
    public static BlockDefinition<RequesterBlock> setupRequester() {
        var blockDef = AEBlocksInvokerMixin.merequester$aeBlock(
            "",
            Utils.getRL(MERequester.REQUESTER_ID),
            RequesterBlock::new,
            (block, properties) -> new AEBaseBlockItem(block, properties.tab(TAB))
        );
        setupRequesterEntity(blockDef.block());
        return blockDef;
    }

    /**
     * logic taken from {@link AEBlockEntities}
     */
    @SuppressWarnings("CastToIncompatibleInterface")
    private static void setupRequesterEntity(RequesterBlock block) {
        AtomicReference<BlockEntityType<RequesterBlockEntity>> typeHolder = new AtomicReference<>();
        BlockEntityType.BlockEntitySupplier<RequesterBlockEntity> supplier = (blockPos, blockState) -> new RequesterBlockEntity(typeHolder.get(), blockPos, blockState);

        @SuppressWarnings("ConstantConditions")
        var type = BlockEntityType.Builder.of(supplier, block).build(null);
        typeHolder.set(type);
        AEBlockEntitiesAccessorMixin.merequester$getBlockEntityTypes().put(Utils.getRL(MERequester.REQUESTER_ID), type);

        AEBaseBlockEntity.registerBlockEntityItem(type, block.asItem());

        BlockEntityTicker<RequesterBlockEntity> serverTicker = null;
        if (ServerTickingBlockEntity.class.isAssignableFrom(RequesterBlockEntity.class)) {
            serverTicker = (level, pos, state, entity) -> {
                ((ServerTickingBlockEntity) entity).serverTick();
            };
        }
        BlockEntityTicker<RequesterBlockEntity> clientTicker = null;
        if (ClientTickingBlockEntity.class.isAssignableFrom(RequesterBlockEntity.class)) {
            clientTicker = (level, pos, state, entity) -> {
                ((ClientTickingBlockEntity) entity).clientTick();
            };
        }

        block.setBlockEntity(RequesterBlockEntity.class, type, clientTicker, serverTicker);
    }

    // TODO: remove this if not required later | AE2 handles registration of the terminal
    public static void registryEvent(RegisterEvent event) {
        // if (event.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
        //     ForgeRegistries.ITEMS.register(TERMINAL.id(), TERMINAL.asItem());
        // }
    }

    private static final class Tab extends CreativeModeTab {

        private Tab() {
            super(BuildConfig.MOD_ID);
        }

        @Override
        public ItemStack makeIcon() {
            return MERequester.TERMINAL.stack();
        }
    }
}
