package com.almostreliable.merequester.requester;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class StorageManager implements IStorageWatcherNode, INBTSerializable<CompoundTag> {

    private final RequesterBlockEntity host;
    private final Storage[] storages;
    @Nullable private IStackWatcher stackWatcher;

    StorageManager(RequesterBlockEntity host) {
        this.host = host;
        storages = new Storage[RequesterBlockEntity.SIZE];
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
        if (amount == 0) return;
        for (var index = 0; index < storages.length; index++) {
            if (key.equals(host.getRequests().getKey(index))) {
                get(index).knownAmount = amount;
                get(index).pendingAmount = 0;
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
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var index = 0; index < storages.length; index++) {
            tag.put(String.valueOf(index), get(index).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var index = 0; index < storages.length; index++) {
            get(index).deserializeNBT(tag.getCompound(String.valueOf(index)));
        }
    }

    // TODO: implement this AE-like
    // void dropContents() {
    //     assert owner.getLevel() != null;
    //     for (var storage : storages) {
    //         if (storage == null) continue;
    //         var itemType = storage.getItemType();
    //         if (!(itemType instanceof AEItemKey aeItem)) continue;
    //         var amount = storage.getBufferAmount() + storage.pendingAmount;
    //         if (amount <= 0) continue;
    //         for (var i = amount; i > 0; i -= 64) {
    //             var stack = aeItem.toStack((int) Math.min(i, 64));
    //             owner.getLevel().addFreshEntity(new ItemEntity(
    //                 owner.getLevel(),
    //                 owner.getBlockPos().getX() + 0.5,
    //                 owner.getBlockPos().getY() + 0.5,
    //                 owner.getBlockPos().getZ() + 0.5,
    //                 stack
    //             ));
    //         }
    //     }
    // }

    void clear(int index) {
        get(index).knownAmount = -1;
        computeKnownAmount(index);
        resetWatcher();
    }

    private void populateWatcher(IStackWatcher watcher) {
        for (var index = 0; index < storages.length; index++) {
            if (host.getRequests().getKey(index) != null) {
                watcher.add(host.getRequests().getKey(index));
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

    public static class Storage implements INBTSerializable<CompoundTag> {

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
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            if (key != null) tag.put(KEY_ID, key.toTagGeneric());
            tag.putLong(BUFFER_AMOUNT_ID, bufferAmount);
            tag.putLong(PENDING_AMOUNT_ID, pendingAmount);
            tag.putLong(KNOWN_AMOUNT_ID, knownAmount);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
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
