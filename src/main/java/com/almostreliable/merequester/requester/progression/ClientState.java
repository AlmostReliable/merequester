package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public record ClientState(RequestStatus type) implements ProgressionState {

    @Override
    public ProgressionState handle(RequesterBlockEntity host, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        throw new UnsupportedOperationException();
    }
}
