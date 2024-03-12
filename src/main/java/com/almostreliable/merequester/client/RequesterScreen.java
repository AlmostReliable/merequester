package com.almostreliable.merequester.client;

import appeng.client.gui.style.ScreenStyle;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.requester.RequesterMenu;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import static com.almostreliable.merequester.Utils.f;

public class RequesterScreen extends AbstractRequesterScreen<RequesterMenu> {

    private static final ResourceLocation TEXTURE = Utils.getRL(f("textures/gui/{}.png", MERequester.REQUESTER_ID));
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 114, GUI_WIDTH, GUI_FOOTER_HEIGHT);
    private static final int MAX_ROW_COUNT = 10;

    @Nullable private RequesterReference requesterReference;

    public RequesterScreen(
        RequesterMenu menu, Inventory playerInventory, Component name, ScreenStyle style
    ) {
        super(menu, playerInventory, name, style, TEXTURE);
    }

    @Override
    protected void init() {
        var possibleRows = (height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        rowAmount = Mth.clamp(possibleRows, MIN_ROW_COUNT, Math.min(Platform.getRequestLimit(), MAX_ROW_COUNT));
        super.init();
    }

    @Override
    protected void clear() {}

    @Override
    protected void refreshList() {
        if (requesterReference != null) {
            lines.clear();
            lines.ensureCapacity(Platform.getRequestLimit());
            for (var i = 0; i < requesterReference.getRequests().size(); i++) {
                lines.add(requesterReference.getRequests().get(i));
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
    protected RequesterReference getById(long requesterId, String name, long sortBy) {
        if (requesterReference == null) {
            requesterReference = new RequesterReference(requesterId, name, sortBy);
            refreshList = true;
        }
        return requesterReference;
    }

    @Override
    protected Rect2i getFooterBounds() {
        return FOOTER_BBOX;
    }
}
