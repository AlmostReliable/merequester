package com.almostreliable.merequester.mixin.registration;

import appeng.init.InitMenuTypes;
import com.almostreliable.merequester.requester.RequesterMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Mixin(InitMenuTypes.class)
public abstract class InitMenuTypesMixin {
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void merequester$initMenuTypes(IForgeRegistry<MenuType<?>> registry, CallbackInfo ci) {
        merequester$registerAll(
            registry,
            RequesterMenu.TYPE,
            RequesterTerminalMenu.TYPE
        );
    }

    @Invoker(value = "registerAll", remap = false)
    private static void merequester$registerAll(IForgeRegistry<MenuType<?>> registry, MenuType<?>... types) {
        throw new AssertionError();
    }
}
