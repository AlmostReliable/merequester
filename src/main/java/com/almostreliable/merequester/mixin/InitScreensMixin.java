package com.almostreliable.merequester.mixin;

import appeng.init.client.InitScreens;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Mixin(InitScreens.class)
public abstract class InitScreensMixin {
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void merequester$initScreens(CallbackInfo ci) {
        // TODO: move this to a client registration class
        InitScreens.register(
            RequesterTerminalMenu.TYPE,
            RequesterTerminalScreen::new,
            "/screens/requester_terminal.json"
        );
    }
}
