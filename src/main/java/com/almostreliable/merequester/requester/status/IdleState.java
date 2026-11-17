package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.ticking.TickRateModulation;

public class IdleState implements StatusState {

    IdleState() {}

    @Override
    public StatusState handle(RequesterBlockEntity host, int index) {
        var storage = host.getStorageManager().get(index);
        if (storage.getBufferAmount() > 0) {
            return EXPORT;
        }

        var request = host.getRequestManager().get(index);
        // knownAmount stays -1 until the storage watcher reports a real count.
        if (request.isRequesting() && storage.getKnownAmount() >= 0 && request.getAmount() > storage.getKnownAmount()) {
            return REQUEST;
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
