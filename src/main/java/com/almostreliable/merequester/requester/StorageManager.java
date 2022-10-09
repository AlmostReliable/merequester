package com.almostreliable.merequester.requester;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class StorageManager implements IStorageWatcherNode, INBTSerializable<CompoundTag> {

    private final RequesterBlockEntity host;
    private final Storage[] storages;
    @Nullable private IStackWatcher stackWatcher;

    public StorageManager(RequesterBlockEntity host) {
        this.host = host;
        storages = new Storage[RequesterBlockEntity.SLOTS];
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
    public void onStackChange(AEKey what, long amount) {
        if (amount == 0) return;
        for (var slot = 0; slot < storages.length; slot++) {
            if (host.getRequests().matches(slot, what)) {
                get(slot).knownAmount = amount;
                get(slot).pendingAmount = 0;
            }
        }
    }

    public long computeDelta(int slot) {
        var request = host.getRequests().get(slot);
        if (request.getStack().isEmpty()) {
            return 0;
        }

        var storedAmount = get(slot).knownAmount + get(slot).pendingAmount;
        if (storedAmount < request.getCount()) {
            return request.getBatch();
        }
        return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var slot = 0; slot < storages.length; slot++) {
            tag.put(String.valueOf(slot), get(slot).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var slot = 0; slot < storages.length; slot++) {
            get(slot).deserializeNBT(tag.getCompound(String.valueOf(slot)));
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

    void clear(int slot) {
        get(slot).knownAmount = -1;
        calcSlotAmount(slot);
        resetWatcher();
    }

    private void populateWatcher(IStackWatcher watcher) {
        for (var slot = 0; slot < storages.length; slot++) {
            if (!host.getRequests().get(slot).getStack().isEmpty()) {
                watcher.add(AEItemKey.of(host.getRequests().get(slot).getStack()));
            }
        }
    }

    private void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            populateWatcher(stackWatcher);
        }
    }

    private void calcSlotAmount(int slot) {
        var request = host.getRequests().get(slot);
        if (request.getStack().isEmpty()) {
            return;
        }
        var genericStack = GenericStack.fromItemStack(request.getStack());
        if (genericStack == null) {
            return;
        }
        get(slot).knownAmount = host.getMainNodeGrid()
            .getStorageService()
            .getInventory()
            .getAvailableStacks()
            .get(genericStack.what());
    }

    public static class Storage implements INBTSerializable<CompoundTag> {

        // serialization IDs
        private static final String ITEM_TYPE_ID = "item_type";
        private static final String BUFFER_AMOUNT_ID = "buffer_amount";
        private static final String PENDING_AMOUNT_ID = "pending_amount";
        private static final String KNOWN_AMOUNT_ID = "known_amount";

        @Nullable private AEKey itemType;
        private long bufferAmount;
        private long pendingAmount;
        private long knownAmount = -1;

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            if (itemType != null) tag.put(ITEM_TYPE_ID, itemType.toTagGeneric());
            tag.putLong(BUFFER_AMOUNT_ID, bufferAmount);
            tag.putLong(PENDING_AMOUNT_ID, pendingAmount);
            tag.putLong(KNOWN_AMOUNT_ID, knownAmount);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains(ITEM_TYPE_ID)) itemType = AEKey.fromTagGeneric(tag.getCompound(ITEM_TYPE_ID));
            bufferAmount = tag.getLong(BUFFER_AMOUNT_ID);
            pendingAmount = tag.getLong(PENDING_AMOUNT_ID);
            knownAmount = tag.getLong(KNOWN_AMOUNT_ID);
        }

        /**
         * @param inserted amount of items inserted into the system
         * @return true if the buffer is not empty
         */
        public boolean compute(long inserted) {
            pendingAmount = inserted;
            bufferAmount = getBufferAmount() - inserted;
            if (bufferAmount == 0) {
                itemType = null;
            }
            return bufferAmount > 0;
        }

        void update(AEKey itemType, long bufferAmount) {
            if (this.itemType != null && !itemType.fuzzyEquals(this.itemType, FuzzyMode.IGNORE_ALL)) {
                throw new IllegalArgumentException("itemType mismatch");
            }
            this.itemType = itemType;
            this.bufferAmount += bufferAmount;
        }

        @Nullable
        public AEKey getItemType() {
            return itemType;
        }

        public long getBufferAmount() {
            return itemType == null ? 0 : bufferAmount;
        }

        public long getKnownAmount() {
            return knownAmount;
        }
    }
}
