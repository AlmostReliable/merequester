package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class RequestState implements ProgressionState {

    RequestState() {}

    @Override
    public ProgressionState handle(RequesterBlockEntity owner, int slot) {

        var craftRequests = owner.getRequests();

        var toCraft = owner.getStorageManager().computeDelta(slot);
        if (toCraft <= 0) {
            return ProgressionState.IDLE;
        }

        var stack = craftRequests.get(slot).toGenericStack(toCraft);
        var future = owner.getMainNodeGrid()
            .getCraftingService()
            .beginCraftingCalculation(
                owner.getLevel(),
                owner::getActionSource,
                stack.what(),
                stack.amount(),
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
