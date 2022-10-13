package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.Requests;

public final class RequestTracker {

    private final long id;
    private final long sortBy;
    private final String name;
    private final Requests server;
    private final Requests client;

    RequestTracker(RequesterBlockEntity requester, long id) {
        this.id = id;
        this.sortBy = requester.getSortValue();
        this.name = requester.getTerminalName().getString();
        this.server = requester.getRequests();
        this.client = new Requests();
    }

    public long getId() {
        return id;
    }

    long getSortBy() {
        return sortBy;
    }

    public String getName() {
        return name;
    }

    public Requests getServer() {
        return server;
    }

    public Requests getClient() {
        return client;
    }
}
