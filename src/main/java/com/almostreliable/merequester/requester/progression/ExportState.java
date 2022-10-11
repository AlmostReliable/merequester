package com.almostreliable.merequester.requester.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.StorageHelper;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class ExportState implements ProgressionState {

    ExportState() {}

    @Override
    public ProgressionState handle(RequesterBlockEntity host, int index) {
        var storageManager = host.getStorageManager().get(index);
        if (storageManager.getKey() == null) {
            return ProgressionState.IDLE;
        }

        var inserted = StorageHelper.poweredInsert(
            host.getMainNodeGrid().getEnergyService(),
            host.getMainNodeGrid().getStorageService().getInventory(),
            storageManager.getKey(),
            storageManager.getBufferAmount(),
            host.getActionSource(),
            Actionable.MODULATE
        );

        if (storageManager.compute(inserted)) {
            return this;
        }
        if (inserted > 0) {
            return ProgressionState.REQUEST;
        }
        return ProgressionState.IDLE;
    }

    @Override
    public RequestStatus type() {
        return RequestStatus.EXPORT;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
