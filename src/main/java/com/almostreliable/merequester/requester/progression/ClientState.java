package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public record ClientState(ProgressionType type) implements IProgressionState {

    @Override
    public IProgressionState handle(RequesterBlockEntity host, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        throw new UnsupportedOperationException();
    }
}
