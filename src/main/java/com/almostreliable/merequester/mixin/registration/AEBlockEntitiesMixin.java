package com.almostreliable.merequester.mixin.registration;

import appeng.core.definitions.AEBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@SuppressWarnings("ALL")
@Mixin(AEBlockEntities.class)
public interface AEBlockEntitiesMixin {
    @Accessor(value = "BLOCK_ENTITY_TYPES", remap = false)
    public static Map<ResourceLocation, BlockEntityType<?>> merequester$getBlockEntityTypes() {
        throw new AssertionError();
    }
}
