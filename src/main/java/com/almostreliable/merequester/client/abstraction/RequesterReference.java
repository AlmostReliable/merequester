package com.almostreliable.merequester.client.abstraction;

import appeng.client.gui.me.interfaceterminal.InterfaceRecord;
import com.almostreliable.merequester.requester.Requests;
import com.almostreliable.merequester.requester.abstraction.RequestHost;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * yoinked from {@link InterfaceRecord}
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

    public RequesterReference(long requesterId, String name, long sortBy) {
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
        return TextComponent.EMPTY;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getSearchName() {
        return searchName;
    }
}
