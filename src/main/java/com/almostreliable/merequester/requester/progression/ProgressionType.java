package com.almostreliable.merequester.requester.progression;

public enum ProgressionType {
    LINK, PLAN, EXPORT, IDLE, REQUEST;

    public ProgressionType translateToClient() {
        if (this == REQUEST || this == PLAN) return IDLE;
        return this;
    }

    public boolean locksSlot() {
        return this == LINK || this == EXPORT;
    }
}
