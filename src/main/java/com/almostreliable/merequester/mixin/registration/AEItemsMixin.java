package com.almostreliable.merequester.mixin.registration;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@SuppressWarnings("ALL")
@Mixin(AEItems.class)
public interface AEItemsMixin {
    @Invoker(value = "item", remap = false)
    static <T extends Item> ItemDefinition<T> merequester$partItem(
        String name, ResourceLocation id, Function<Item.Properties, T> partFactory, CreativeModeTab creativeTab
    ) {
        throw new AssertionError();
    }
}
