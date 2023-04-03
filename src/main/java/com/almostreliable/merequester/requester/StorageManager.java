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
import java.util.*;

public class StorageManager implements IStorageWatcherNode, TagSerializable<CompoundTag> {

    private final RequesterBlockEntity host;
    private final Storage[] storages;
    private final Map<Integer, List<AEKey>> missingIngreds;
    @Nullable private IStackWatcher stackWatcher;

    StorageManager(RequesterBlockEntity host) {
        this.host = host;
        storages = new Storage[Platform.getRequestLimit()];
        missingIngreds = new HashMap<>(Platform.getRequestLimit());
        for (var i = 0; i < Platform.getRequestLimit(); i++) {
            missingIngreds.put(i, new ArrayList<>());
        }
    }

    public Storage get(int index) {
        if (storages[index] == null) {
            storages[index] = new Storage();
        }
        return storages[index];
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
            if (!missingIngreds.get(i).isEmpty()) {
                missingIngreds.get(i).remove(key);
            }
        }
    }

    public long computeAmountToCraft(int index) {
        var requests = host.getRequests();
        if (requests.getKey(index) == null) return 0;

        var storedAmount = get(index).knownAmount + get(index).pendingAmount;
        if (storedAmount < requests.getAmount(index)) {
            return requests.get(index).getBatch();
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

    void clear(int index) {
        get(index).knownAmount = -1;
        missingIngreds.get(index).clear();
        computeKnownAmount(index);
        resetWatcher();
    }

    public void addMissingIngred(int index, Set<AEKey> missingIngred) {
        missingIngreds.get(index).addAll(missingIngred);
        resetWatcher();
    }

    public boolean hasMissingIngred(int index) {
        return !missingIngreds.get(index).isEmpty();
    }

    private void populateWatcher(IStackWatcher watcher) {
        for (var i = 0; i < storages.length; i++) {
            if (host.getRequests().getKey(i) != null) {
                watcher.add(host.getRequests().getKey(i));
            }
            if (!missingIngreds.get(i).isEmpty()) {
                for (var key : missingIngreds.get(i)) {
                    watcher.add(key);
                }
            }
        }
    }

    private void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            populateWatcher(stackWatcher);
        }
    }

    private void computeKnownAmount(int index) {
        var key = host.getRequests().getKey(index);
        if (key == null) return;
        get(index).knownAmount = host.getMainNodeGrid()
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

        @Nullable private AEKey key;
        private long bufferAmount;
        private long pendingAmount;
        private long knownAmount = -1;

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
            pendingAmount = inserted;
            bufferAmount = getBufferAmount() - inserted;
            if (bufferAmount == 0) {
                key = null;
            }
            return bufferAmount > 0;
        }

        void update(AEKey key, long bufferAmount) {
            if (this.key != null && !key.fuzzyEquals(this.key, FuzzyMode.PERCENT_99)) {
                throw new IllegalArgumentException("storage key mismatch");
            }
            this.key = key;
            this.bufferAmount += bufferAmount;
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
