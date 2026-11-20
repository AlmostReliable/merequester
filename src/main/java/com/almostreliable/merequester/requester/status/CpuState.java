package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

public class CpuState extends BlockingState {

    CpuState() {}

    @Override
    public RequestStatus type() {
        return RequestStatus.CPU;
    }

    @Override
    boolean canRetry(RequesterBlockEntity host) {
        for (var cpu : host.getMainNodeGrid().getCraftingService().getCpus()) {
            if (!cpu.isBusy()) return true;
        }

        return false;
    }
}
