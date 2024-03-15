package com.almostreliable.merequester.client;

import appeng.menu.slot.FakeSlot;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.RequestDisplay;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.mixin.accessors.SlotMixin;
import com.almostreliable.merequester.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class RequestSlot extends FakeSlot {

    private final RequestDisplay host;
    private final RequesterReference requesterReference;
    private final int slot;

    private boolean isLocked;

    public RequestSlot(RequestDisplay host, RequesterReference requesterReference, int slot, int x, int y) {
        super(requesterReference.getRequests(), slot);
        this.host = host;
        this.requesterReference = requesterReference;
        this.slot = slot;
        Utils.cast(this, SlotMixin.class).merequester$setX(x);
        Utils.cast(this, SlotMixin.class).merequester$setY(y);
    }

    @Override
    public void increase(ItemStack is) {}

    @Override
    public void decrease(ItemStack is) {}

    @Override
    public boolean hasItem() {
        // hide item tooltip when locked
        return !isLocked && super.hasItem();
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(ItemStack carried) {
        if (isLocked) {
            return Collections.singletonList(Utils.translate("tooltip", "locked").withStyle(ChatFormatting.RED));
        }
        // custom tooltip for fluid containers
        var emptyingTooltip = host.getEmptyingTooltip(this, carried);
        if (emptyingTooltip == null) return super.getCustomTooltip(carried);
        return emptyingTooltip;
    }

    @Override
    public final int getMaxStackSize() {
        return 0;
    }

    public RequesterReference getRequesterReference() {
        return requesterReference;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public void setFilterTo(ItemStack itemStack) {
        Platform.sendDragAndDrop(getRequesterReference().getRequesterId(), getSlot(), itemStack);
    }
}
