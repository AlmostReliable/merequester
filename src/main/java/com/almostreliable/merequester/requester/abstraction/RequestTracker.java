package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.requester.Request;
import com.almostreliable.merequester.requester.RequestManager;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

/**
 * Simplified representation of a {@link Request} and its parent {@link RequesterBlockEntity}
 * for synchronization in menus.
 */
public final class RequestTracker {

    private final long id;
    private final long sortBy;
    private final String name;
    private final RequestManager server;
    private final RequestManager client;

    RequestTracker(RequesterBlockEntity requester, long id) {
        this.id = id;
        this.sortBy = requester.getSortValue();
        this.name = requester.getTerminalName().getString();
        this.server = requester.getRequestManager();
        this.client = new RequestManager(server.size());
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

    public RequestManager getServer() {
        return server;
    }

    public RequestManager getClient() {
        return client;
    }
}
