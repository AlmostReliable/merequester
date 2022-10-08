package com.almostreliable.merequester.requester;

import appeng.api.inventories.InternalInventory;
import appeng.client.gui.me.patternaccess.PatternProviderRecord;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.network.chat.Component;

/**
 * yoinked from {@link PatternProviderRecord}
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class RequesterRecord implements InternalInventoryHost, Comparable<RequesterRecord> {

    private final long serverId;
    private final long order;
    private final String displayName;
    private final String searchName;
    private final Requests requests;

    public RequesterRecord(long serverId, long order, Component name) {
        this.serverId = serverId;
        this.order = order;
        this.displayName = name.getString();
        this.searchName = displayName.toLowerCase();
        requests = new Requests(this);
    }

    @Override
    public void saveChanges() {}

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {}

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int compareTo(RequesterRecord o) {
        return Long.compare(order, o.order);
    }

    public long getServerId() {
        return serverId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSearchName() {
        return searchName;
    }

    public Requests getRequests() {
        return requests;
    }
}
