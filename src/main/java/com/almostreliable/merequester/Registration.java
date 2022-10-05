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
import com.almostreliable.merequester.mixin.AEBlockEntitiesMixin;
import com.almostreliable.merequester.mixin.AEBlocksMixin;
import com.almostreliable.merequester.mixin.AEItemsMixin;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import com.almostreliable.merequester.utils.Utils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.concurrent.atomic.AtomicReference;

public final class Registration {

    private Registration() {}

    private static final Tab TAB = new Tab();

    public static ItemDefinition<PartItem<RequesterTerminalPart>> setupTerminal() {
        PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminalPart.class));
        return AEItemsMixin.merequester$partItem(
            "",
            Utils.getRL(MERequester.TERMINAL_ID),
            props -> new PartItem<>(props, RequesterTerminalPart.class, RequesterTerminalPart::new),
            TAB
        );
    }

    public static BlockDefinition<RequesterBlock> setupRequester() {
        var blockDef = AEBlocksMixin.merequester$aeBlock(
            "",
            Utils.getRL(MERequester.REQUESTER_ID),
            RequesterBlock::new,
            (block, properties) -> new AEBaseBlockItem(block, properties.tab(TAB))
        );
        setupRequesterEntity(blockDef);
        return blockDef;
    }

    /**
     * logic taken from {@link AEBlockEntities}
     */
    @SuppressWarnings("CastToIncompatibleInterface")
    private static void setupRequesterEntity(BlockDefinition<RequesterBlock> block) {
        AtomicReference<BlockEntityType<RequesterBlockEntity>> typeHolder = new AtomicReference<>();
        BlockEntityType.BlockEntitySupplier<RequesterBlockEntity> supplier = (blockPos, blockState) -> new RequesterBlockEntity(typeHolder.get(), blockPos, blockState);

        @SuppressWarnings("ConstantConditions")
        var type = BlockEntityType.Builder.of(supplier, block.block()).build(null);
        typeHolder.set(type);
        AEBlockEntitiesMixin.merequester$getBlockEntityTypes().put(Utils.getRL(MERequester.REQUESTER_ID), type);

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

        block.block().setBlockEntity(RequesterBlockEntity.class, type, clientTicker, serverTicker);
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
