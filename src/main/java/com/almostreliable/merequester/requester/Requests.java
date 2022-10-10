package com.almostreliable.merequester.requester;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.almostreliable.merequester.Utils.f;

public class Requests implements InternalInventory, INBTSerializable<CompoundTag> {

    // if null, the inventory is client-side and doesn't need saving
    @Nullable private final InternalInventoryHost host;
    private final Request[] requests;

    public Requests(@Nullable InternalInventoryHost host) {
        this.host = host;
        requests = new Request[RequesterBlockEntity.SLOTS];
        for (var i = 0; i < requests.length; i++) {
            requests[i] = new Request(this, i);
        }
    }

    public Requests() {
        this(null);
    }

    @Override
    public int size() {
        return RequesterBlockEntity.SLOTS;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return get(slot).stack;
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        insertItem(slot, stack, false);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (host == null || host.isClientSide()) {
            get(slot).updateStackClient(stack);
        } else {
            get(slot).updateStack(stack);
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    public Request get(int slot) {
        return requests[slot];
    }

    public int firstAvailableSlot() {
        for (var slot = 0; slot < requests.length; slot++) {
            var request = get(slot);
            if (request.stack.isEmpty()) return slot;
        }
        return -1;
    }

    public boolean matches(int slot, AEKey what) {
        return what.matches(GenericStack.fromItemStack(get(slot).stack));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var i = 0; i < size(); i++) {
            tag.put(String.valueOf(i), get(i).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var i = 0; i < size(); i++) {
            get(i).deserializeNBT(tag.getCompound(String.valueOf(i)));
        }
    }

    public final class Request implements INBTSerializable<CompoundTag> {

        // serialization IDs
        private static final String STATE_ID = "state";
        private static final String STACK_ID = "stack";
        private static final String COUNT_ID = "count";
        private static final String BATCH_ID = "batch";

        private final InternalInventory requestHost;
        private final int slot;
        private boolean state = true;
        private ItemStack stack = ItemStack.EMPTY;
        private long count;
        private long batch = 1;

        private Request(InternalInventory requestHost, int slot) {
            this.requestHost = requestHost;
            this.slot = slot;
        }

        public GenericStack toGenericStack(long count) {
            var stackCopy = stack.copy();
            return new GenericStack(Objects.requireNonNull(AEItemKey.of(stackCopy)), count);
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putBoolean(STATE_ID, state);
            tag.put(STACK_ID, stack.serializeNBT());
            tag.putLong(COUNT_ID, count);
            tag.putLong(BATCH_ID, batch);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            state = tag.getBoolean(STATE_ID);
            stack = ItemStack.of(tag.getCompound(STACK_ID));
            count = tag.getLong(COUNT_ID);
            batch = tag.getLong(BATCH_ID);
        }

        public void updateState(boolean state) {
            if (this.state != state) {
                this.state = state;
                if (host != null) host.saveChanges();
            }
        }

        public void updateCount(long count) {
            var oldStack = stack;
            var oldCount = this.count;
            var oldBatch = batch;
            if (stack.isEmpty() || count <= 0) {
                resetSlot();
            } else {
                this.count = count;
            }
            if ((!oldStack.sameItem(stack) || oldCount != this.count || oldBatch != batch) && host != null) {
                host.saveChanges();
            }
        }

        public void updateBatch(long batch) {
            var oldBatch = this.batch;
            this.batch = batch <= 0 ? 1 : batch;
            if (oldBatch != this.batch && host != null) host.saveChanges();
        }

        @Override
        public String toString() {
            return f("Request[state={}, stack={}, count={}, batch={}]", state, stack, count, batch);
        }

        public boolean isDifferent(Request other) {
            return state != other.state ||
                !ItemStack.isSameItemSameTags(stack, other.stack) ||
                count != other.count ||
                batch != other.batch;
        }

        private void updateStackClient(ItemStack stack) {
            this.stack = stack;
        }

        private void updateStack(ItemStack stack) {
            var oldStack = this.stack;
            if (stack.isEmpty()) {
                if (!oldStack.isEmpty()) resetSlot();
                return;
            }
            if (oldStack.sameItem(stack)) {
                if (count != stack.getCount()) count = stack.getCount();
                return;
            }
            count = stack.getCount();
            this.stack = stack;
            stack.setCount(1);
            batch = 1;
            stackChanged();
        }

        private void stackChanged() {
            if (host != null) host.onChangeInventory(requestHost, slot);
        }

        private void resetSlot() {
            var oldStack = stack;
            stack = ItemStack.EMPTY;
            count = 0;
            batch = 1;
            if (!oldStack.isEmpty()) stackChanged();
        }

        public int getSlot() {
            return slot;
        }

        @OnlyIn(Dist.CLIENT)
        public InternalInventoryHost getRequesterReference() {
            assert host != null;
            return host;
        }

        public boolean getState() {
            return state;
        }

        public ItemStack getStack() {
            return stack;
        }

        public long getCount() {
            return count;
        }

        public long getBatch() {
            return batch;
        }

        public boolean isRequesting() {
            return state && !stack.isEmpty();
        }
    }
}
