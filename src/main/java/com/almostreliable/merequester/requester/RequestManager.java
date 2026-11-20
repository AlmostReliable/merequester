package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.requester.abstraction.RequestHost;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.neoforged.neoforge.common.util.ValueIOSerializable;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RequestManager implements ValueIOSerializable {

    private final Request[] requests;
    private final RequesterConfigInventory configInventory;

    public RequestManager(@Nullable RequestHost host, int size) {
        this.requests = new Request[size];
        for (var i = 0; i < requests.length; i++) {
            requests[i] = new Request(host, i);
        }
        this.configInventory = new RequesterConfigInventory(this);
    }

    public RequestManager(int size) {
        this(null, size);
    }

    public Request get(int index) {
        return requests[index];
    }

    public int size() {
        return requests.length;
    }

    public boolean isInvalidIndex(int index) {
        return index < 0 || index >= requests.length;
    }

    @Nullable
    public GenericStack getStack(int index) {
        return get(index).toGenericStack();
    }

    @Nullable
    public AEKey getKey(int index) {
        return get(index).getKey();
    }

    public long getAmount(int index) {
        return get(index).getAmount();
    }

    void setStack(int index, @Nullable GenericStack stack) {
        get(index).updateKey(stack);
    }

    public InternalInventory getConfigInventory() {
        return configInventory;
    }

    @Override
    public void serialize(ValueOutput data) {
        for (var i = 0; i < size(); i++) {
            get(i).serialize(data.child(String.valueOf(i)));
        }
    }

    @Override
    public void deserialize(ValueInput data) {
        for (var i = 0; i < size(); i++) {
            get(i).deserialize(data.childOrEmpty(String.valueOf(i)));
        }
    }

    public void fromComponent(List<Request.Component> exportedRequests) {
        for (var i = 0; i < size(); i++) {
            if (i < exportedRequests.size()) {
                get(i).fromComponent(exportedRequests.get(i));
            } else {
                setStack(i, null);
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
}
