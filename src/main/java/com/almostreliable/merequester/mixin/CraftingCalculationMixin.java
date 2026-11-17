package com.almostreliable.merequester.mixin;

import com.almostreliable.merequester.requester.DeferredCraftingProviderUpdates;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.RequesterPlanActivity;

import com.google.common.base.Stopwatch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.crafting.CraftingCalculation;

/**
 * AE2 runs a crafting plan on the calculator thread, then {@code simulateFor} parks the server
 * thread until the plan yields. Requester plans skip that wait. The plan still pauses itself
 * when its time budget is spent, and the next level tick wakes it.
 */
@Mixin(value = CraftingCalculation.class, remap = false)
public class CraftingCalculationMixin {

    @Shadow
    private ICraftingSimulationRequester simRequester;

    @Shadow
    private Object monitor;

    @Shadow
    private Stopwatch watch;

    @Shadow
    private boolean running;

    @Shadow
    private boolean done;

    @Shadow
    private int time;

    @Unique
    private boolean merequester$awake;

    @Inject(method = "simulateFor", at = @At("HEAD"), cancellable = true, remap = false)
    private void merequester$simulateOffServerThread(int micros, CallbackInfoReturnable<Boolean> cir) {
        if (!merequester$isRequesterPlan()) return;

        // Plans are still paused here. Apply queued pattern updates before this tick wakes them.
        DeferredCraftingProviderUpdates.flush();

        synchronized (this.monitor) {
            if (this.done) {
                merequester$markAsleep();
                cir.setReturnValue(false);
                return;
            }

            // A running plan already has a budget. Refreshing it here would keep the worker awake across ticks.
            if (!this.running) {
                this.time = micros;
                if (this.watch.isRunning()) this.watch.stop();
                this.watch.reset();
                this.watch.start();
                this.running = true;
                merequester$markAwake();
                this.monitor.notifyAll();
            }
        }

        cir.setReturnValue(true);
    }

    @Inject(
        method = {"handlePausing", "finish"},
        at = @At(
            value = "FIELD",
            target = "Lappeng/crafting/CraftingCalculation;running:Z",
            opcode = org.objectweb.asm.Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void merequester$onPlanPaused(CallbackInfo ci) {
        merequester$markAsleep();
    }

    @Unique
    private void merequester$markAwake() {
        if (this.merequester$awake || !merequester$isRequesterPlan()) return;
        this.merequester$awake = true;
        RequesterPlanActivity.enter();
    }

    @Unique
    private void merequester$markAsleep() {
        if (!this.merequester$awake) return;
        this.merequester$awake = false;
        RequesterPlanActivity.exit();
    }

    @Unique
    private boolean merequester$isRequesterPlan() {
        if (this.simRequester == null) return false;
        var source = this.simRequester.getActionSource();
        return source != null && source.machine().orElse(null) instanceof RequesterBlockEntity;
    }
}
