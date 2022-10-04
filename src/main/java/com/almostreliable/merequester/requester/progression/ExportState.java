package com.almostreliable.merequester.requester.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.StorageHelper;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class ExportState implements IProgressionState {

    ExportState() {}

    @Override
    public IProgressionState handle(RequesterBlockEntity host, int slot) {
        var storageManager = host.getStorageManager().get(slot);
        if (storageManager.getItemType() == null) {
            return IProgressionState.IDLE;
        }

        var inserted = StorageHelper.poweredInsert(
            host.getMainNodeGrid().getEnergyService(),
            host.getMainNodeGrid().getStorageService().getInventory(),
            storageManager.getItemType(),
            storageManager.getBufferAmount(),
            host.getActionSource(),
            Actionable.MODULATE
        );

        if (storageManager.compute(inserted)) {
            return this;
        }
        if (inserted > 0) {
            return IProgressionState.REQUEST;
        }
        return IProgressionState.IDLE;
    }

    @Override
    public ProgressionType type() {
        return ProgressionType.EXPORT;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
