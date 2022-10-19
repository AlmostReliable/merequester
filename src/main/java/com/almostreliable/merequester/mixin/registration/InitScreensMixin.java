package com.almostreliable.merequester.mixin.registration;

import appeng.init.client.InitScreens;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.client.RequesterScreen;
import com.almostreliable.merequester.client.RequesterTerminalScreen;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.requester.RequesterMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.almostreliable.merequester.wireless.WirelessRequesterTerminalMenu;
import com.almostreliable.merequester.wireless.WirelessRequesterTerminalScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.almostreliable.merequester.Utils.f;

@SuppressWarnings("ALL")
@Mixin(InitScreens.class)
public abstract class InitScreensMixin {
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void merequester$initScreens(CallbackInfo ci) {
        InitScreens.register(
            RequesterMenu.TYPE,
            RequesterScreen::new,
            f("/screens/{}.json", MERequester.REQUESTER_ID)
        );
        InitScreens.<RequesterTerminalMenu, RequesterTerminalScreen<RequesterTerminalMenu>>register(
            RequesterTerminalMenu.TYPE,
            RequesterTerminalScreen::new,
            f("/screens/{}.json", MERequester.TERMINAL_ID)
        );
        if(!Platform.isModLoaded("ae2wtlib")) InitScreens.register(WirelessRequesterTerminalMenu.TYPE,
            WirelessRequesterTerminalScreen::new, f("/screens/{}.json", WirelessRequesterTerminalMenu.ID));
    }
}
