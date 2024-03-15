package com.almostreliable.merequester.mixin.accessors;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("ALL")
@Mixin(EditBox.class)
public interface EditBoxMixin {

    @Accessor("isEditable")
    boolean merequester$isEditable();
}
