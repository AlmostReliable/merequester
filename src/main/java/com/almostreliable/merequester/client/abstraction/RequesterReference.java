package com.almostreliable.merequester.client.abstraction;

import com.almostreliable.merequester.requester.RequestManager;
import com.almostreliable.merequester.requester.abstraction.RequestHost;

import net.minecraft.network.chat.Component;

/**
 * yoinked from AE2's legacy {@code PatternProviderRecord}
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class RequesterReference implements RequestHost, Comparable<RequesterReference> {

    private final long requesterId;
    private final String displayName;
    private final String searchName;
    private final long sortBy;
    private final RequestManager requestManager;

    public RequesterReference(long requesterId, String name, long sortBy, int requestCount) {
        this.requesterId = requesterId;
        this.displayName = name;
        this.searchName = name.toLowerCase();
        this.sortBy = sortBy;
        requestManager = new RequestManager(this, requestCount);
    }

    // <editor-fold defaultstate="collapsed" desc="Not important for a reference.">
    @Override
    public void saveChanges() {}

    @Override
    public void requestChanged(int index) {}

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public Component getTerminalName() {
        return Component.empty();
    }
    // </editor-fold>

    @Override
    public RequestManager getRequestManager() {
        return requestManager;
    }

    @Override
    public int compareTo(RequesterReference o) {
        return Long.compare(sortBy, o.sortBy);
    }

    public long getRequesterId() {
        return requesterId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSearchName() {
        return searchName;
    }
}
