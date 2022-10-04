package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public record CraftingLinkState(ICraftingLink link) implements IProgressionState {

    @Override
    public IProgressionState handle(RequesterBlockEntity host, int slot) {
        if (link.isDone()) {
            return IProgressionState.EXPORT;
        }

        if (link.isCanceled()) {
            return IProgressionState.IDLE;
        }

        return this;
    }

    @Override
    public ProgressionType type() {
        return ProgressionType.LINK;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SAME;
    }
}
