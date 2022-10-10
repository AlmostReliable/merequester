package com.almostreliable.merequester.mixin;

import appeng.client.gui.WidgetContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@SuppressWarnings("ALL")
@Mixin(WidgetContainer.class)
public interface WidgetContainerMixin {
    @Accessor(value = "widgets", remap = false)
    Map<String, AbstractWidget> merequester$getWidgets();
}
