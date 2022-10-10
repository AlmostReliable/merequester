package com.almostreliable.merequester.requester.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public interface IProgressionState {

    IProgressionState IDLE = new IdleState();
    IProgressionState REQUEST = new RequestState();
    IProgressionState EXPORT = new ExportState();

    /**
     * @param host the entity that is hosting the progression.
     * @param slot the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    IProgressionState handle(RequesterBlockEntity host, int slot);

    ProgressionType type();

    TickRateModulation getTickRateModulation();
}
