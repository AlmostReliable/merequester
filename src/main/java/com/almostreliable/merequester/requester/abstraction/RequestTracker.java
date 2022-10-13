package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.Requests;
import com.almostreliable.merequester.requester.Requests.Request;

/**
 * Simplified representation of a {@link Request} and its parent {@link RequesterBlockEntity}
 * for synchronization in menus.
 */
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
