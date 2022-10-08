package com.almostreliable.merequester.terminal;

import appeng.menu.slot.AppEngSlot;
import com.almostreliable.merequester.requester.RequesterRecord;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RequestSlot extends AppEngSlot {

    private final RequesterRecord host;
    private final int slot;
    private final int x;
    private final int y;

    public int getSlot() {
        return slot;
    }

    public RequestSlot(RequesterRecord host, int slot, int x, int y) {
        super(host.getRequests(), slot);
        this.host = host;
        this.slot = slot;
        this.x = x;
        this.y = y;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {}

    @Override
    public final ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public final boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public final void set(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
        }

        super.set(stack);
    }

    @Override
    public final boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void initialize(ItemStack stack) {
    }

    @Override
    public final int getMaxStackSize() {
        return 0;
    }

    public boolean canSetFilterTo() {
        return slot < getInventory().size();
    }

    public RequesterRecord getHost() {
        return host;
    }
}
