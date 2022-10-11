package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public interface ProgressionState {

    ProgressionState IDLE = new IdleState();
    ProgressionState REQUEST = new RequestState();
    ProgressionState EXPORT = new ExportState();

    /**
     * @param host the entity that is hosting the progression.
     * @param slot the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    ProgressionState handle(RequesterBlockEntity host, int slot);

    RequestStatus type();

    TickRateModulation getTickRateModulation();
}
