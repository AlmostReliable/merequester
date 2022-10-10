package com.almostreliable.merequester.client;

import appeng.api.inventories.InternalInventory;
import appeng.client.gui.me.patternaccess.PatternProviderRecord;
import appeng.util.inv.InternalInventoryHost;
import com.almostreliable.merequester.requester.Requests;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * yoinked from {@link PatternProviderRecord}
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@OnlyIn(Dist.CLIENT)
public class RequesterReference implements InternalInventoryHost, Comparable<RequesterReference> {

    private final long requesterId;
    private final String displayName;
    private final String searchName;
    private final long sortBy;
    private final Requests requests;

    RequesterReference(long requesterId, String name, long sortBy) {
        this.requesterId = requesterId;
        this.displayName = name;
        this.searchName = name.toLowerCase();
        this.sortBy = sortBy;
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
    public int compareTo(RequesterReference o) {
        return Long.compare(sortBy, o.sortBy);
    }

    public long getRequesterId() {
        return requesterId;
    }

    String getDisplayName() {
        return displayName;
    }

    String getSearchName() {
        return searchName;
    }

    Requests getRequests() {
        return requests;
    }
}
