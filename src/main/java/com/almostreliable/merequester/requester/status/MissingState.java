package com.almostreliable.merequester.requester.status;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class MissingState implements StatusState {

    MissingState() {}

    @Override
    public StatusState handle(RequesterBlockEntity host, int slot) {
        if (host.getStorageManager().hasMissingIngred(slot)) {
            return this;
        }
        return StatusState.IDLE;
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.MISSING;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
