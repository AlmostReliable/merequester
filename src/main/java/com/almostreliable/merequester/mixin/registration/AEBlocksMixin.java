package com.almostreliable.merequester.mixin.registration;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("ALL")
@Mixin(AEBlocks.class)
public interface AEBlocksMixin {

    @Invoker(value = "block", remap = false)
    public static <T extends Block> BlockDefinition<T> merequester$aeBlock(
        String name, ResourceLocation id, Supplier<T> blockSupplier, @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory
    ) {
        throw new AssertionError();
    }
}
