package com.almostreliable.merequester.requester.status;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public record LinkState(ICraftingLink link) implements StatusState {

    @Override
    public StatusState handle(RequesterBlockEntity host, int slot) {
        if (link.isDone()) {
            return StatusState.EXPORT;
        }

        if (link.isCanceled()) {
            return StatusState.IDLE;
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
