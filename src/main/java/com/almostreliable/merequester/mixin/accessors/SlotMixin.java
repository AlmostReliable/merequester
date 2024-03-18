package com.almostreliable.merequester.mixin.accessors;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("ALL")
@Mixin(Slot.class)
public interface SlotMixin {

    @Accessor("x")
    void merequester$setX(int x);

    @Accessor("y")
    void merequester$setY(int y);
}
