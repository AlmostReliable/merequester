package com.almostreliable.merequester.requester.status;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class PlanState implements StatusState {

    private final Future<? extends ICraftingPlan> future;

    PlanState(Future<? extends ICraftingPlan> future) {
        this.future = future;
    }

    @Override
    public StatusState handle(RequesterBlockEntity host, int index) {
        if (!future.isDone()) return this;
        if (future.isCancelled()) return StatusState.IDLE;

        try {
            var plan = future.get();
            var submitResult = host.getMainNodeGrid().getCraftingService().submitJob(plan, host, null, false, host.getActionSource());

            if (!submitResult.successful() || submitResult.link() == null) {
                if (submitResult.errorCode() == CraftingSubmitErrorCode.INCOMPLETE_PLAN && !plan.missingItems().isEmpty()) {
                    return StatusState.MISSING;
                }
                return StatusState.IDLE;
            }

            host.getStorageManager().get(index).setTotalAmount(plan.finalOutput().amount());
            return new LinkState(Objects.requireNonNull(submitResult.link()));
        } catch (InterruptedException | ExecutionException e) {
            return StatusState.IDLE;
        }
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.PLAN;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
