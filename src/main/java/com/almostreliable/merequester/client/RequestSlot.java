package com.almostreliable.merequester.client;

import appeng.menu.slot.FakeSlot;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.mixin.SlotMixin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class RequestSlot extends FakeSlot {

    private final RequestDisplay host;
    private final RequesterReference requesterReference;
    private final int slot;

    RequestSlot(RequestDisplay host, RequesterReference requesterReference, int slot, int x, int y) {
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
    public final int getMaxStackSize() {
        return 0;
    }

    public RequesterReference getRequesterReference() {
        return requesterReference;
    }

    public int getSlot() {
        return slot;
    }

    // render custom tooltip for fluid containers
    @Nullable
    @Override
    public List<Component> getCustomTooltip(Function<ItemStack, List<Component>> getItemTooltip, ItemStack carried) {
        var emptyingTooltip = host.getEmptyingTooltip(this, carried);
        if (emptyingTooltip == null) return super.getCustomTooltip(getItemTooltip, carried);
        return emptyingTooltip;
    }
}
