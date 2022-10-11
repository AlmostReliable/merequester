package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class IdleState implements ProgressionState {

    IdleState() {}

    @Override
    public ProgressionState handle(RequesterBlockEntity host, int index) {
        if (host.getStorageManager().get(index).getBufferAmount() > 0) {
            return ProgressionState.EXPORT;
        }

        var request = host.getRequests().get(index);
        if (request.isRequesting() && request.getAmount() > host.getStorageManager().get(index).getKnownAmount()) {
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
