package com.almostreliable.merequester.terminal;

import appeng.menu.slot.FakeSlot;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.mixin.SlotMixin;
import com.almostreliable.merequester.requester.RequesterRecord;
import net.minecraft.world.item.ItemStack;

public class RequestSlot extends FakeSlot {

    private final RequesterRecord host;
    private final int slot;

    public RequestSlot(RequesterRecord host, int slot, int x, int y) {
        super(host.getRequests(), slot);
        this.host = host;
        this.slot = slot;
        Utils.cast(this, SlotMixin.class).merequester$setX(x);
        Utils.cast(this, SlotMixin.class).merequester$setY(y);
    }

    @Override
    public final int getMaxStackSize() {
        return 0;
    }

    @Override
    public void increase(ItemStack is) {}

    @Override
    public void decrease(ItemStack is) {}

    public RequesterRecord getHost() {
        return host;
    }

    public int getSlot() {
        return slot;
    }
}
