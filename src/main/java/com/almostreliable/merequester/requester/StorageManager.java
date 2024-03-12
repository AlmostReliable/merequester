package com.almostreliable.merequester.requester;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEKey;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.platform.TagSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import java.util.List;

public class StorageManager implements IStorageWatcherNode, TagSerializable<CompoundTag> {

    private final RequesterBlockEntity host;
    private final Storage[] storages;
    @Nullable private IStackWatcher stackWatcher;

    StorageManager(RequesterBlockEntity host) {
        this.host = host;
        storages = new Storage[Platform.getRequestLimit()];
    }

    public Storage get(int slot) {
        if (storages[slot] == null) {
            storages[slot] = new Storage();
        }
        return storages[slot];
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        stackWatcher = newWatcher;
        resetWatcher();
    }

    @Override
    public void onStackChange(AEKey key, long amount) {
        for (var i = 0; i < storages.length; i++) {
            if (key.equals(host.getRequests().getKey(i))) {
                get(i).knownAmount = amount;
                get(i).pendingAmount = 0;
            }
        }
    }

    public long computeAmountToCraft(int slot) {
        var requests = host.getRequests();
        if (requests.getKey(slot) == null) return 0;

        var storedAmount = get(slot).knownAmount + get(slot).pendingAmount;
        if (storedAmount < requests.getAmount(slot)) {
            return requests.get(slot).getBatch();
        }
        return 0;
    }

    @Override
    public CompoundTag serialize() {
        var tag = new CompoundTag();
        for (var i = 0; i < storages.length; i++) {
            tag.put(String.valueOf(i), get(i).serialize());
        }
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        for (var i = 0; i < storages.length; i++) {
            get(i).deserialize(tag.getCompound(String.valueOf(i)));
        }
    }

    void addDrops(List<ItemStack> drops) {
        for (var storage : storages) {
            if (storage == null || storage.key == null) continue;
            storage.key.addDrops(
                storage.getBufferAmount() + storage.pendingAmount,
                drops,
                host.getLevel(),
                host.getBlockPos()
            );
        }
    }

    void clear(int slot) {
        get(slot).knownAmount = -1;
        computeKnownAmount(slot);
        resetWatcher();
    }

    private void populateWatcher(IStackWatcher watcher) {
        for (var i = 0; i < storages.length; i++) {
            if (host.getRequests().getKey(i) != null) {
                watcher.add(host.getRequests().getKey(i));
            }
        }
    }

    private void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            populateWatcher(stackWatcher);
        }
    }

    private void computeKnownAmount(int slot) {
        var key = host.getRequests().getKey(slot);
        if (key == null) return;
        get(slot).knownAmount = host.getMainNodeGrid()
            .getStorageService()
            .getInventory()
            .getAvailableStacks()
            .get(key);
    }

    public static class Storage implements TagSerializable<CompoundTag> {

        // serialization IDs
        private static final String KEY_ID = "key";
        private static final String BUFFER_AMOUNT_ID = "buffer_amount";
        private static final String PENDING_AMOUNT_ID = "pending_amount";
        private static final String KNOWN_AMOUNT_ID = "known_amount";

        @Nullable private AEKey key; // the item or fluid type stored in this storage
        private long totalAmount; // total amount of the job that needs to be exported
        private long bufferAmount; // amount of items or fluid in the buffer
        private long pendingAmount; // amount currently being inserted into the system but did not arrive yet
        private long knownAmount = -1; // the known amount stored in the system

        @Override
        public CompoundTag serialize() {
            var tag = new CompoundTag();
            if (key != null) tag.put(KEY_ID, key.toTagGeneric());
            tag.putLong(BUFFER_AMOUNT_ID, bufferAmount);
            tag.putLong(PENDING_AMOUNT_ID, pendingAmount);
            tag.putLong(KNOWN_AMOUNT_ID, knownAmount);
            return tag;
        }

        @Override
        public void deserialize(CompoundTag tag) {
            key = tag.contains(KEY_ID) ? AEKey.fromTagGeneric(tag.getCompound(KEY_ID)) : null;
            bufferAmount = tag.getLong(BUFFER_AMOUNT_ID);
            pendingAmount = tag.getLong(PENDING_AMOUNT_ID);
            knownAmount = tag.getLong(KNOWN_AMOUNT_ID);
        }

        /**
         * @param inserted amount of items or fluid inserted into the system
         * @return true if the buffer is not empty
         */
        public boolean compute(long inserted) {
            pendingAmount += inserted;
            bufferAmount = getBufferAmount() - inserted;
            totalAmount -= inserted;
            if (bufferAmount == 0) {
                key = null;
            }
            return bufferAmount > 0 || totalAmount > 0;
        }

        void update(AEKey key, long bufferAmount) {
            if (this.key != null && !key.fuzzyEquals(this.key, FuzzyMode.PERCENT_99)) {
                throw new IllegalArgumentException("storage key mismatch");
            }
            this.key = key;
            this.bufferAmount += bufferAmount;
        }

        public void setTotalAmount(long totalAmount) {
            this.totalAmount = totalAmount;
        }

        @Nullable
        public AEKey getKey() {
            return key;
        }

        public long getBufferAmount() {
            return key == null ? 0 : bufferAmount;
        }

        public long getKnownAmount() {
            return knownAmount;
        }
    }
}
