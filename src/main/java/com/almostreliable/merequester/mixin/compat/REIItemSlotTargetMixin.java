package com.almostreliable.merequester.mixin.compat;

import appeng.helpers.InventoryAction;
import appeng.menu.slot.AppEngSlot;
import com.almostreliable.merequester.client.RequestSlot;
import com.almostreliable.merequester.network.DragAndDropPacket;
import com.almostreliable.merequester.network.PacketHandler;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("ALL")
@Mixin(targets = "appeng.integration.modules.rei.GhostIngredientHandler$ItemSlotTarget")
public class REIItemSlotTargetMixin {

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
    private void merequester$acceptItem(
        DraggableStack ingredient, CallbackInfoReturnable<Boolean> ci, EntryStack<?> entryStack, ItemStack itemStack
    ) {
        if (slot instanceof RequestSlot requestSlot) {
            PacketHandler.CHANNEL.sendToServer(new DragAndDropPacket(
                requestSlot.getRequesterReference().getRequesterId(),
                requestSlot.getSlot(),
                InventoryAction.SET_FILTER,
                itemStack
            ));
            ci.setReturnValue(true);
        }
    }

    @Inject(
        method = "accept",
        at = @At(value = "INVOKE", target = "Lappeng/core/sync/network/NetworkHandler;instance()Lappeng/core/sync/network/NetworkHandler;", ordinal = 1),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true,
        remap = false
    )
    private void merequester$acceptFluid(
        DraggableStack ingredient, CallbackInfoReturnable<Boolean> ci, EntryStack<?> entryStack, FluidStack fluidStack,
        ItemStack wrappedFluid
    ) {
        if (slot instanceof RequestSlot requestSlot) {
            PacketHandler.CHANNEL.sendToServer(new DragAndDropPacket(
                requestSlot.getRequesterReference().getRequesterId(),
                requestSlot.getSlot(),
                InventoryAction.SET_FILTER,
                wrappedFluid
            ));
            ci.setReturnValue(true);
        }
    }
}
