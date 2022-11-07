package com.almostreliable.merequester.requester.status;

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
            var link = host.getMainNodeGrid()
                .getCraftingService()
                .submitJob(plan, host, null, false, host.getActionSource());

            if (link == null) {
                return StatusState.IDLE;
            }

            return new LinkState(Objects.requireNonNull(link));
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
