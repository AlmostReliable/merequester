package com.almostreliable.merequester.requester.status;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public interface StatusState {

    StatusState IDLE = new IdleState();
    StatusState REQUEST = new RequestState();
    StatusState EXPORT = new ExportState();

    /**
     * @param host the entity that is hosting the progression.
     * @param slot the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    StatusState handle(RequesterBlockEntity host, int slot);

    RequestStatus type();

    TickRateModulation getTickRateModulation();
}
