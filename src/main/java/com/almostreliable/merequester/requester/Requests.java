package com.almostreliable.merequester.requester;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;

public class Requests implements InternalInventory, INBTSerializable<CompoundTag> {

    private final InternalInventoryHost host;
    private final Request[] requests;

    public Requests(InternalInventoryHost host) {
        this.host = host;
        requests = new Request[RequesterBlockEntity.SLOTS];
        for (var i = 0; i < requests.length; i++) {
            requests[i] = new Request(this, i);
        }
    }

    @Override
    public int size() {
        return RequesterBlockEntity.SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return get(slot).stack;
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        if (host.isClientSide()) {
            get(slot).updateStackClient(stack);
        } else {
            get(slot).updateStack(stack);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        throw new UnsupportedOperationException();
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
        // TODO: implement syncing for screen rendering
        // private boolean changed;

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
                host.saveChanges();
                // changed = true;
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
            if (!oldStack.sameItem(stack) || oldCount != this.count || oldBatch != batch) {
                host.saveChanges();
                // changed = true;
            }
        }

        public void updateBatch(long batch) {
            var oldBatch = this.batch;
            this.batch = batch <= 0 ? 1 : batch;
            if (oldBatch != this.batch) {
                host.saveChanges();
                // changed = true;
            }
        }

        @Override
        public String toString() {
            return "Request[" + "state=" + state + ", " + "stack=" + stack + ", " + "count=" + count + ", " + "batch=" + batch + ']';
        }

        // @Override
        // public void encode(FriendlyByteBuf buffer) {
        //     buffer.writeInt(slot);
        //     buffer.writeBoolean(state);
        //     buffer.writeItemStack(stack, true);
        //     buffer.writeLong(count);
        //     buffer.writeLong(batch);
        //     changed = false;
        // }
        //
        // @Override
        // public void decode(FriendlyByteBuf buffer) {
        //     if (slot != buffer.readInt()) {
        //         throw new IllegalStateException("Slot mismatch");
        //     }
        //     state = buffer.readBoolean();
        //     stack = buffer.readItem();
        //     count = buffer.readLong();
        //     batch = buffer.readLong();
        //     ClientHandler.updateRequestGui(slot);
        // }
        //
        // @Override
        // public boolean hasChanged() {
        //     return changed;
        // }

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
                if (count != stack.getCount()) {
                    count = stack.getCount();
                    // changed = true;
                }
                return;
            }
            count = stack.getCount();
            this.stack = stack;
            stack.setCount(1);
            batch = 1;
            stackChanged();
        }

        private void stackChanged() {
            host.onChangeInventory(requestHost, slot);
            // changed = true;
        }

        private void resetSlot() {
            var oldStack = stack;
            stack = ItemStack.EMPTY;
            count = 0;
            batch = 1;
            if (!oldStack.isEmpty()) stackChanged();
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

        long getBatch() {
            return batch;
        }

        public boolean isRequesting() {
            return state && !stack.isEmpty();
        }
    }
}
