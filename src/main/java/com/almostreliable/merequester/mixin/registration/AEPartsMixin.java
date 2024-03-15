package com.almostreliable.merequester.mixin.registration;

import appeng.core.definitions.AEParts;
import com.almostreliable.merequester.Registration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Mixin(AEParts.class)
public abstract class AEPartsMixin {

    /**
     * AEParts are always initialized last on Forge and Fabric,
     * so we can safely init our registry here.
     */
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private static void merequester$initRegistration(CallbackInfo ci) {
        Registration.init();
    }
}
