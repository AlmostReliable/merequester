package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public record CraftingLinkState(ICraftingLink link) implements ProgressionState {

    @Override
    public ProgressionState handle(RequesterBlockEntity host, int slot) {
        if (link.isDone()) {
            return ProgressionState.EXPORT;
        }

        if (link.isCanceled()) {
            return ProgressionState.IDLE;
        }

        return this;
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.LINK;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SAME;
    }
}
