package com.almostreliable.merequester.client;

import appeng.client.gui.me.patternaccess.PatternProviderRecord;
import com.almostreliable.merequester.requester.RequestHost;
import com.almostreliable.merequester.requester.Requests;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * yoinked from {@link PatternProviderRecord}
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@OnlyIn(Dist.CLIENT)
public class RequesterReference implements RequestHost, Comparable<RequesterReference> {

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
    public Requests getRequests() {
        return requests;
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
}
