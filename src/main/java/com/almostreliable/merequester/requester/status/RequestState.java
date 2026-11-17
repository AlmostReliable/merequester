package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.CraftingPlanBudget;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;

public class RequestState implements StatusState {

    RequestState() {}

    @Override
    public StatusState handle(RequesterBlockEntity owner, int index) {
        var request = owner.getRequestManager().get(index);
        if (!request.getState()) return IDLE;

        var storage = owner.getStorageManager().get(index);
        // The storage watcher has not reported a count yet. -1 would look like a deficit.
        if (storage.getKnownAmount() < 0) return IDLE;

        var amountToCraft = owner.getStorageManager().computeAmountToCraft(index);
        if (amountToCraft <= 0) return IDLE;

        var key = request.getKey();
        var level = owner.getLevel();
        if (key == null || level == null) return IDLE;

        var crafting = owner.getMainNodeGrid().getCraftingService();
        if (!crafting.isCraftable(key)) {
            return BlockingState.coolDown(owner, new MissingState());
        }

        // Stay in this state so the next tick can try again without queueing a plan.
        if (!CraftingPlanBudget.hasRoom(level)) return REQUEST;

        var future = crafting.beginCraftingCalculation(
            level,
            owner::getActionSource,
            key,
            amountToCraft,
            CalculationStrategy.REPORT_MISSING_ITEMS
        );
        CraftingPlanBudget.track(level, future);
        return new PlanState(future);
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.REQUEST;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SLOWER;
    }
}
