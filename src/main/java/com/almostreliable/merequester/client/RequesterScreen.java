package com.almostreliable.merequester.client;

import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.requester.RequesterMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.ScreenStyle;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public class RequesterScreen extends AbstractRequesterScreen<RequesterMenu> {

    private static final int MAX_ROW_COUNT = 10;

    @Nullable
    private RequesterReference requesterReference;

    public RequesterScreen(RequesterMenu menu, Inventory playerInventory, Component name, ScreenStyle style) {
        super(menu, playerInventory, name, style);
    }

    @Override
    protected void init() {
        var possibleRows = (height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        rowAmount = Mth.clamp(possibleRows, MIN_ROW_COUNT, Math.min(menu.getRequestCount(), MAX_ROW_COUNT));
        super.init();
    }

    @Override
    protected void clear() {}

    @Override
    protected void refreshList() {
        if (requesterReference != null) {
            var requests = requesterReference.getRequestManager();
            lines.clear();
            lines.ensureCapacity(requests.size());
            for (var i = 0; i < requests.size(); i++) {
                lines.add(requests.get(i));
            }
        }
        refreshList = false;
        resetScrollbar();
    }

    @Override
    protected Set<RequesterReference> getByName(String name) {
        if (requesterReference == null || !requesterReference.getDisplayName().equals(name)) {
            throw new IllegalStateException("reference is null or name doesn't match");
        }
        return Collections.singleton(requesterReference);
    }

    @Override
    protected RequesterReference getById(long requesterId, String name, long sortBy, int requestCount) {
        if (requesterReference == null) {
            requesterReference = new RequesterReference(requesterId, name, sortBy, requestCount);
            refreshList = true;
        }
        return requesterReference;
    }
}
