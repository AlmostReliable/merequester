package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class RequestState implements IProgressionState {

    RequestState() {}

    @Override
    public IProgressionState handle(RequesterBlockEntity owner, int slot) {

        var craftRequests = owner.getRequests();

        var toCraft = owner.getStorageManager().computeDelta(slot);
        if (toCraft <= 0) {
            return IProgressionState.IDLE;
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
    public ProgressionType type() {
        return ProgressionType.REQUEST;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SLOWER;
    }
}
