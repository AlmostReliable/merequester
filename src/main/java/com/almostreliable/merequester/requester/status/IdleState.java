package com.almostreliable.merequester.requester.status;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class IdleState implements StatusState {

    IdleState() {}

    @Override
    public StatusState handle(RequesterBlockEntity host, int index) {
        if (host.getStorageManager().get(index).getBufferAmount() > 0) {
            return StatusState.EXPORT;
        }

        var request = host.getRequests().get(index);
        if (request.isRequesting() && request.getAmount() > host.getStorageManager().get(index).getKnownAmount()) {
            return StatusState.REQUEST;
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
