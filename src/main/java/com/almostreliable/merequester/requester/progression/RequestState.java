package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class RequestState implements ProgressionState {

    RequestState() {}

    @Override
    public ProgressionState handle(RequesterBlockEntity owner, int index) {
        var requests = owner.getRequests();

        var amountToCraft = owner.getStorageManager().computeAmountToCraft(index);
        if (amountToCraft <= 0) return ProgressionState.IDLE;
        var key = requests.getKey(index);

        var future = owner.getMainNodeGrid()
            .getCraftingService()
            .beginCraftingCalculation(
                owner.getLevel(),
                owner::getActionSource,
                key,
                amountToCraft,
                CalculationStrategy.CRAFT_LESS
            );

        return new CraftingPlanState(future);
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
