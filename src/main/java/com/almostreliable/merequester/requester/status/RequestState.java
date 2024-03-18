package com.almostreliable.merequester.requester.status;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class RequestState implements StatusState {

    RequestState() {}

    @Override
    public StatusState handle(RequesterBlockEntity owner, int index) {
        var amountToCraft = owner.getStorageManager().computeAmountToCraft(index);
        if (amountToCraft <= 0) return StatusState.IDLE;
        var key = owner.getRequests().getKey(index);

        var future = owner
            .getMainNodeGrid()
            .getCraftingService()
            .beginCraftingCalculation(
                owner.getLevel(),
                owner::getActionSource,
                key,
                amountToCraft,
                CalculationStrategy.REPORT_MISSING_ITEMS
            );

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
