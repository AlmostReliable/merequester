package com.almostreliable.merequester.requester.abstraction;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.networking.IGrid;
import appeng.api.stacks.GenericStack;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public abstract class AbstractRequesterMenu extends AEBaseMenu {

    public static final String SORT_BY_ID = "sort_by";
    public static final String UNIQUE_NAME_ID = "unique_name";

    // used to give requesters unique IDs
    private long idSerial = Long.MIN_VALUE;

    protected AbstractRequesterMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
        createPlayerInventorySlots(playerInventory);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        RequestTracker requestTracker = getRequestTracker(id);
        if (requestTracker == null) return;

        if (slot < 0 || slot >= requestTracker.getServer().size()) {
            MERequester.LOGGER.warn("Requester Screen refers to invalid slot {} of {}", slot, requestTracker.getName());
            return;
        }

        var requestSlot = requestTracker.getServer().getSlotInv(slot);
        var requestStack = requestSlot.getStackInSlot(0);
        var carriedStack = getCarried();

        // the screen only has fake slots, so don't transfer anything to the player's inventory
        switch (action) {
            case PICKUP_OR_SET_DOWN ->
                requestSlot.setItemDirect(0, carriedStack.isEmpty() ? ItemStack.EMPTY : carriedStack.copy());
            case SPLIT_OR_PLACE_SINGLE -> {
                if (carriedStack.isEmpty()) {
                    requestSlot.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    var copy = carriedStack.copy();
                    copy.setCount(1);
                    requestSlot.setItemDirect(0, copy);
                }
            }
            case SHIFT_CLICK -> requestSlot.setItemDirect(0, ItemStack.EMPTY);
            case EMPTY_ITEM -> {
                var emptyingAction = ContainerItemStrategies.getEmptyingAction(carriedStack);
                if (emptyingAction != null) {
                    requestSlot.insertItem(
                        0,
                        GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()),
                        false
                    );
                }
            }
            case CREATIVE_DUPLICATE -> {
                if (player.getAbilities().instabuild && carriedStack.isEmpty()) {
                    if (requestStack.isEmpty()) {
                        setCarried(ItemStack.EMPTY);
                    } else {
                        var stack = requestStack.copy();
                        stack.setCount(stack.getMaxStackSize());
                        setCarried(stack);
                    }
                }
            }
            default -> {
            }
        }
    }

    public void applyDragAndDrop(ServerPlayer player, int requestIndex, long requesterId, ItemStack item) {
        setCarried(item);
        doAction(player, InventoryAction.PICKUP_OR_SET_DOWN, requestIndex, requesterId);
        setCarried(ItemStack.EMPTY);
    }

    public void updateRequesterState(long requesterId, int requestIndex, boolean state) {
        var requestTracker = getRequestTracker(requesterId);
        if (requestTracker == null) return;
        var request = requestTracker.getServer().get(requestIndex);
        request.updateState(state);
    }

    public void updateRequesterNumbers(long requesterId, int requestIndex, long amount, long batch) {
        var requestTracker = getRequestTracker(requesterId);
        if (requestTracker == null) return;
        var request = requestTracker.getServer().get(requestIndex);
        request.updateAmount(amount);
        request.updateBatch(batch);
    }

    protected abstract void sendFullUpdate(@Nullable IGrid grid);

    protected abstract void sendPartialUpdate();

    protected void syncRequestTrackerFull(RequestTracker requestTracker) {
        var server = requestTracker.getServer();
        var client = requestTracker.getClient();

        // get the requests from the server
        var tag = server.serialize();
        // store the information in the client tracker to
        // check for differences on partial updates later
        // tag serialization is used to avoid references to the original data
        client.deserialize(tag);

        // send relevant data to the client
        tag.putString(UNIQUE_NAME_ID, requestTracker.getName());
        tag.putLong(SORT_BY_ID, requestTracker.getSortBy());
        Platform.sendInventoryData(getPlayer(), requestTracker.getId(), tag);
    }

    protected void syncRequestTrackerPartial(RequestTracker requestTracker) {
        var server = requestTracker.getServer();
        var client = requestTracker.getClient();

        CompoundTag tag = null;
        // iterate through the server data and check for differences
        for (var i = 0; i < server.size(); i++) {
            var serverRequest = server.get(i);
            var clientRequest = client.get(i);

            if (serverRequest.isDifferent(clientRequest)) {
                // write initial data as soon as something is different
                if (tag == null) {
                    tag = new CompoundTag();
                    tag.putString(UNIQUE_NAME_ID, requestTracker.getName());
                    tag.putLong(SORT_BY_ID, requestTracker.getSortBy());
                }

                var serverData = serverRequest.serialize();
                tag.put(String.valueOf(i), serverData);
                // update the client information for future difference checks
                clientRequest.deserialize(serverData);
            }
        }

        // only send an update if something changed
        if (tag != null) {
            Platform.sendInventoryData(getPlayer(), requestTracker.getId(), tag);
        }
    }

    protected RequestTracker createTracker(RequesterBlockEntity requester) {
        RequestTracker requestTracker = new RequestTracker(requester, idSerial);
        idSerial++;
        return requestTracker;
    }

    @Nullable
    protected abstract RequestTracker getRequestTracker(long id);
}
