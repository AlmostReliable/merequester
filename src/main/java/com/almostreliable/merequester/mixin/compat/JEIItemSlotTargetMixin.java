package com.almostreliable.merequester.mixin.compat;

import appeng.menu.slot.AppEngSlot;
import com.almostreliable.merequester.client.RequestSlot;
import com.almostreliable.merequester.platform.Platform;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("ALL")
@Mixin(targets = "appeng.integration.modules.jei.GhostIngredientHandler$ItemSlotTarget")
public abstract class JEIItemSlotTargetMixin {

    @Shadow(remap = false)
    @Final
    private AppEngSlot slot;

    @Inject(
        method = "accept",
        at = @At(value = "INVOKE", target = "Lappeng/core/sync/network/NetworkHandler;instance()Lappeng/core/sync/network/NetworkHandler;"),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true,
        remap = false
    )
    private void merequester$accept(Object ingredient, CallbackInfo ci, ItemStack wrapped) {
        if (slot instanceof RequestSlot requestSlot) {
            Platform.sendDragAndDrop(
                requestSlot.getRequesterReference().getRequesterId(),
                requestSlot.getSlot(),
                wrapped
            );
            ci.cancel();
        }
    }
}
