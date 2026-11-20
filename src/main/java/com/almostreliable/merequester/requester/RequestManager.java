package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.requester.abstraction.RequestHost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the same approach as {@link GenericStackInv} to track items and fluids.
 * <p>
 * Automatically provides a menu wrapper by implementing {@link InternalInventory}.
 */
@SuppressWarnings("UnstableApiUsage")
public class RequestManager implements MEStorage, GenericInternalInventory, InternalInventory, INBTSerializable<CompoundTag> {

    // if null, the inventory is client-side and doesn't need saving
    @Nullable
    private final RequestHost host;
    private final Request[] requests;

    public RequestManager(@Nullable RequestHost host, int size) {
        this.host = host;
        requests = new Request[size];
        for (var i = 0; i < requests.length; i++) {
            requests[i] = new Request(host, i);
        }
    }

    public RequestManager(int size) {
        this(null, size);
    }

    public Request get(int index) {
        return requests[index];
    }

    @Override
    public int size() {
        return requests.length;
    }

    public boolean isInvalidIndex(int index) {
        return index < 0 || index >= requests.length;
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
    public boolean isSupportedType(AEKeyType type) {
        return true;
    }

    @Override
    public boolean isAllowedIn(int slot, AEKey what) {
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
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        for (var i = 0; i < size(); i++) {
            tag.put(String.valueOf(i), get(i).serializeNBT(registries));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        for (var i = 0; i < size(); i++) {
            get(i).deserializeNBT(registries, tag.getCompound(String.valueOf(i)));
        }
    }

    public void fromComponent(List<Request.Component> exportedRequests) {
        for (var i = 0; i < size(); i++) {
            if (i < exportedRequests.size()) {
                get(i).fromComponent(exportedRequests.get(i));
            } else {
                setItemDirect(i, ItemStack.EMPTY);
            }
        }
    }

    public List<Request.Component> toComponent() {
        var result = new ArrayList<Request.Component>(size());
        for (var i = 0; i < size(); i++) {
            result.add(get(i).toComponent());
        }
        return result;
    }

    public int firstAvailableIndex() {
        for (var i = 0; i < size(); i++) {
            if (getKey(i) == null) return i;
        }
        return -1;
    }

    @Override
    public Component getDescription() {
        if (host == null) return net.minecraft.network.chat.Component.empty();
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
    public void setItemDirect(int slot, ItemStack stack) {
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
}
