package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class IdleState implements ProgressionState {

    IdleState() {}

    @Override
    public ProgressionState handle(RequesterBlockEntity host, int slot) {
        if (host.getStorageManager().get(slot).getBufferAmount() > 0) {
            return ProgressionState.EXPORT;
        }

        var request = host.getRequests().get(slot);
        if (request.isRequesting() && request.getCount() > host.getStorageManager().get(slot).getKnownAmount()) {
            return ProgressionState.REQUEST;
        }

        return this;
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.IDLE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
