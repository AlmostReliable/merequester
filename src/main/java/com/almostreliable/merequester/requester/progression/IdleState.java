package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class IdleState implements IProgressionState {

    IdleState() {}

    @Override
    public IProgressionState handle(RequesterBlockEntity host, int slot) {
        if (host.getStorageManager().get(slot).getBufferAmount() > 0) {
            return IProgressionState.EXPORT;
        }

        var request = host.getRequests().get(slot);
        if (request.isRequesting() && request.getCount() > host.getStorageManager().get(slot).getKnownAmount()) {
            return IProgressionState.REQUEST;
        }

        return this;
    }

    @Override
    public ProgressionType type() {
        return ProgressionType.IDLE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
