package com.almostreliable.merequester.client;

import appeng.menu.slot.FakeSlot;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.mixin.SlotMixin;
import net.minecraft.world.item.ItemStack;

public class RequestSlot extends FakeSlot {

    private final RequesterReference host;
    private final int slot;

    RequestSlot(RequesterReference host, int slot, int x, int y) {
        super(host.getRequests(), slot);
        this.host = host;
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

    public RequesterReference getHost() {
        return host;
    }

    public int getSlot() {
        return slot;
    }
}
