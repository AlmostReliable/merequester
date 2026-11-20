package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class PlanState implements StatusState {

    private static final Set<CraftingSubmitErrorCode> CPU_ERROR_CODES = Set.of(
        CraftingSubmitErrorCode.NO_CPU_FOUND,
        CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND,
        CraftingSubmitErrorCode.CPU_BUSY,
        CraftingSubmitErrorCode.CPU_OFFLINE,
        CraftingSubmitErrorCode.CPU_TOO_SMALL
    );
    private final Future<? extends ICraftingPlan> future;

    PlanState(Future<? extends ICraftingPlan> future) {
        this.future = future;
    }

    @Override
    public StatusState handle(RequesterBlockEntity host, int index) {
        if (isNotDone()) return this;
        if (future.isCancelled()) return IDLE;

        try {
            var plan = future.get();
            if (!plan.missingItems().isEmpty()) {
                return new MissingState();
            }

            var submitResult = host.getMainNodeGrid().getCraftingService().submitJob(plan, host, null, false, host.getActionSource());
            var craftingLink = submitResult.link();

            if (!submitResult.successful() || craftingLink == null) {
                if (submitResult.errorCode() != null && CPU_ERROR_CODES.contains(submitResult.errorCode())) {
                    return new CpuState();
                }

                return IDLE;
            }

            host.getStorageManager().get(index).setTotalAmount(plan.finalOutput().amount());
            return new LinkState(craftingLink);
        } catch (InterruptedException | ExecutionException e) {
            return IDLE;
        }
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.PLAN;
    }

    boolean isNotDone() {
        return !future.isDone();
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
