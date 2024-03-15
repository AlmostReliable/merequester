package com.almostreliable.merequester;

import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.almostreliable.merequester.mixin.registration.AEBlockEntitiesMixin;
import com.almostreliable.merequester.mixin.registration.AEBlocksMixin;
import com.almostreliable.merequester.mixin.registration.AEItemsMixin;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.concurrent.atomic.AtomicReference;

public final class Registration {

    public static final ItemDefinition<PartItem<?>> TERMINAL = setupTerminal();
    public static final BlockDefinition<RequesterBlock> REQUESTER = setupRequester();

    private Registration() {}

    public static void init() {
        MERequester.LOGGER.info("Registering content");
    }

    private static ItemDefinition<PartItem<?>> setupTerminal() {
        PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminalPart.class));
        return AEItemsMixin.merequester$aeItem(
            "",
            Utils.getRL(MERequester.TERMINAL_ID),
            props -> new PartItem<>(props, RequesterTerminalPart.class, RequesterTerminalPart::new),
            ModTab.TAB_KEY
        );
    }

    private static BlockDefinition<RequesterBlock> setupRequester() {
        var blockDef = AEBlocksMixin.merequester$aeBlock(
            "",
            Utils.getRL(MERequester.REQUESTER_ID),
            RequesterBlock::new,
            AEBaseBlockItem::new
        );
        registerRequesterEntity(blockDef);
        return blockDef;
    }

    private static void registerRequesterEntity(BlockDefinition<RequesterBlock> block) {
        AtomicReference<BlockEntityType<RequesterBlockEntity>> typeHolder = new AtomicReference<>();
        BlockEntityType.BlockEntitySupplier<RequesterBlockEntity> supplier = (blockPos, blockState)
            -> new RequesterBlockEntity(typeHolder.get(), blockPos, blockState);

        @SuppressWarnings("ConstantConditions")
        var type = BlockEntityType.Builder.of(supplier, block.block()).build(null);
        typeHolder.set(type);

        AEBlockEntitiesMixin.merequester$blockEntityTypes().put(Utils.getRL(MERequester.REQUESTER_ID), type);
        AEBaseBlockEntity.registerBlockEntityItem(type, block.asItem());
        block.block().setBlockEntity(RequesterBlockEntity.class, type, null, null);
    }
}
