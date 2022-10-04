package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class CraftingPlanState implements IProgressionState {

    private final Future<? extends ICraftingPlan> future;

    CraftingPlanState(Future<? extends ICraftingPlan> future) {
        this.future = future;
    }

    @Override
    public IProgressionState handle(RequesterBlockEntity host, int slot) {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return IProgressionState.IDLE;
        }

        try {
            var plan = future.get();
            var submitResult = host.getMainNodeGrid()
                .getCraftingService()
                .submitJob(plan, host, null, false, host.getActionSource());

            if (!submitResult.successful() || submitResult.link() == null) {
                return IProgressionState.IDLE;
            }

            return new CraftingLinkState(Objects.requireNonNull(submitResult.link()));
        } catch (InterruptedException | ExecutionException e) {
            return IProgressionState.IDLE;
        }
    }

    @Override
    public ProgressionType type() {
        return ProgressionType.PLAN;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
