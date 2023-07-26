package com.almostreliable.merequester.mixin.accessors;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("ALL")
@Mixin(Screen.class)
public interface ScreenMixin {
    @Accessor("renderables")
    List<Renderable> merequester$getRenderables();
}
