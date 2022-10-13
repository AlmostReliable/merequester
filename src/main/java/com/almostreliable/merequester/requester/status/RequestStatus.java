package com.almostreliable.merequester.requester.status;

public enum RequestStatus {
    LINK, PLAN, EXPORT, IDLE, REQUEST;

    public RequestStatus translateToClient() {
        if (this == REQUEST || this == PLAN) return IDLE;
        return this;
    }

    public boolean locksRequest() {
        return this == LINK || this == EXPORT;
    }
}
