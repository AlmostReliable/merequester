package com.almostreliable.merequester.requester;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import com.almostreliable.merequester.requester.progression.RequestStatus;
import com.google.common.primitives.Ints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.almostreliable.merequester.Utils.f;

/**
 * Uses the same approach as {@link GenericStackInv} to track items and fluids.
 * <p>
 * Automatically provides a menu wrapper by implementing {@link InternalInventory}.
 */
@SuppressWarnings("UnstableApiUsage")
public class Requests implements MEStorage, GenericInternalInventory, InternalInventory, INBTSerializable<CompoundTag> {

    // if null, the inventory is client-side and doesn't need saving
    @Nullable private final RequestHost host;
    private final Request[] requests;

    public Requests(@Nullable RequestHost host) {
        this.host = host;
        requests = new Request[RequesterBlockEntity.SIZE];
        for (var i = 0; i < requests.length; i++) {
            requests[i] = new Request(i);
        }
    }

    public Requests() {
        this(null);
    }

    public Request get(int index) {
        return requests[index];
    }

    @Override
    public int size() {
        return RequesterBlockEntity.SIZE;
    }

    @Nullable
    @Override
    public GenericStack getStack(int index) {
        return get(index).toGenericStack();
    }

    @Nullable
    @Override
    public AEKey getKey(int index) {
        return get(index).getKey();
    }

    @Override
    public long getAmount(int index) {
        return get(index).getAmount();
    }

    @Override
    public long getMaxAmount(AEKey key) {
        return 1;
    }

    @Override
    public long getCapacity(AEKeyType keyType) {
        return 1;
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public void setStack(int index, @Nullable GenericStack stack) {
        get(index).updateKey(stack);
    }

    @Override
    public boolean isAllowed(AEKey key) {
        return true;
    }

    @Override
    public long insert(int index, AEKey key, long amount, Actionable mode) {
        if (mode == Actionable.SIMULATE) return amount;
        if (host == null || host.isClientSide()) {
            get(index).setClientKey(key, amount);
        } else {
            get(index).updateKey(new GenericStack(key, amount));
        }
        return amount;
    }

    @Override
    public long extract(int index, AEKey key, long amount, Actionable mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onChange() {
        if (host != null) host.saveChanges();
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

    public int firstAvailableIndex() {
        for (var i = 0; i < size(); i++) {
            if (getKey(i) == null) return i;
        }
        return -1;
    }

    @Override
    public Component getDescription() {
        if (host == null) return Component.empty();
        return host.getTerminalName();
    }

    // <editor-fold defaultstate="collapsed" desc="Not required for requests.">
    @Override
    public void beginBatch() {}

    @Override
    public void endBatch() {}

    @Override
    public void endBatchSuppressed() {}
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="InternalInventory menu wrapper delegates.">
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.isEmpty() || convertToSuitableStack(stack) != null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        var genericStack = getStack(slot);
        if (genericStack != null && genericStack.what() instanceof AEItemKey itemKey) {
            return itemKey.toStack();
        }
        return GenericStack.wrapInItemStack(genericStack);
    }

    @Override
    public void setItemDirect(int slot, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            setStack(slot, null);
        } else {
            var converted = convertToSuitableStack(stack);
            if (converted != null) setStack(slot, converted);
        }
    }

    @Nullable
    private GenericStack convertToSuitableStack(ItemStack stack) {
        if (stack.isEmpty()) return null;

        var unwrappedStack = GenericStack.unwrapItemStack(stack);
        ItemStack returnStack = stack;
        if (unwrappedStack != null) {
            if (unwrappedStack.what() instanceof AEItemKey itemKey) {
                returnStack = itemKey.toStack(Math.max(1, Ints.saturatedCast(unwrappedStack.amount())));
            } else {
                return unwrappedStack;
            }
        }

        var itemKey = AEItemKey.of(returnStack);
        return itemKey != null ? new GenericStack(itemKey, returnStack.getCount()) : null;
    }
    // </editor-fold>

    public final class Request implements INBTSerializable<CompoundTag> {

        // serialization IDs
        private static final String STATE_ID = "state";
        private static final String KEY_ID = "key";
        private static final String AMOUNT_ID = "amount";
        private static final String BATCH_ID = "batch";
        private static final String STATUS_ID = "status";

        private final int index;

        private boolean state = true;
        @Nullable private AEKey key;
        private long amount;
        private long batch = 1;

        // this status is only relevant for the client
        // the actual request status is stored in the BlockEntity
        private RequestStatus clientStatus;

        private Request(int index) {
            this.index = index;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putBoolean(STATE_ID, state);
            if (key != null) tag.put(KEY_ID, key.toTagGeneric());
            tag.putLong(AMOUNT_ID, amount);
            tag.putLong(BATCH_ID, batch);
            tag.putInt(STATUS_ID, clientStatus.ordinal());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            state = tag.getBoolean(STATE_ID);
            key = tag.contains(KEY_ID) ? AEKey.fromTagGeneric(tag.getCompound(KEY_ID)) : null;
            amount = tag.getLong(AMOUNT_ID);
            batch = tag.getLong(BATCH_ID);
            clientStatus = RequestStatus.values()[tag.getInt(STATUS_ID)];
        }

        public void updateState(boolean state) {
            if (this.state != state) {
                this.state = state;
                if (host != null) host.saveChanges();
            }
        }

        public void updateAmount(long amount) {
            if (key == null || amount <= 0) {
                resetSlot();
                return;
            }
            if (this.amount != amount) {
                this.amount = amount;
                if (host != null) host.saveChanges();
            }
        }

        public void updateBatch(long batch) {
            var oldBatch = this.batch;
            this.batch = Mth.clamp(batch, 1, batch);
            if (oldBatch != this.batch && host != null) host.saveChanges();
        }

        @Override
        public String toString() {
            return f(
                "Request[state={}, key={}, amount={}, batch={}, client_status={}]",
                state, key == null ? "none" : key.getDisplayName(), amount, batch, clientStatus
            );
        }

        public boolean isDifferent(Request clientRequest) {
            return state != clientRequest.state ||
                !Objects.equals(key, clientRequest.key) ||
                amount != clientRequest.amount ||
                batch != clientRequest.batch ||
                clientStatus != clientRequest.clientStatus;
        }

        @Nullable
        private GenericStack toGenericStack() {
            if (key == null) return null;
            return new GenericStack(key, amount);
        }

        private void updateKey(@Nullable GenericStack stack) {
            if (stack == null) {
                if (key != null) resetSlot();
                return;
            }
            if (key != null && key.matches(stack)) {
                if (amount != stack.amount()) updateAmount(stack.amount());
                return;
            }
            key = stack.what();
            amount = stack.amount();
            batch = 1;
            keyChanged();
        }

        private void setClientKey(AEKey key, long amount) {
            this.key = key;
            this.amount = amount;
        }

        private void keyChanged() {
            if (host != null) host.requestChanged(index);
        }

        private void resetSlot() {
            if (key == null && amount == 0) return;
            key = null;
            amount = 0;
            batch = 1;
            keyChanged();
        }

        public int getIndex() {
            return index;
        }

        public boolean getState() {
            return state;
        }

        @Nullable
        private AEKey getKey() {
            return key;
        }

        public long getAmount() {
            return amount;
        }

        public long getBatch() {
            return batch;
        }

        @OnlyIn(Dist.CLIENT)
        public RequestHost getRequesterReference() {
            assert host != null;
            return host;
        }

        @OnlyIn(Dist.CLIENT)
        public RequestStatus getClientStatus() {
            return clientStatus;
        }

        void setClientStatus(RequestStatus clientStatus) {
            this.clientStatus = clientStatus.translateToClient();
        }

        public boolean isRequesting() {
            return state && key != null;
        }
    }
}
