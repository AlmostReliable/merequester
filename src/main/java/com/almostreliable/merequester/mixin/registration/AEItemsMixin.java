package com.almostreliable.merequester.mixin.registration;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.resources.ResourceKey;
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
    public static <T extends Item> ItemDefinition<T> merequester$aeItem(
        String name, ResourceLocation id, Function<Item.Properties, T> partFactory, ResourceKey<CreativeModeTab> creativeTab
    ) {
        throw new AssertionError();
    }
}
