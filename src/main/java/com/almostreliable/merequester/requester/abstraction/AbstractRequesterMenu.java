package com.almostreliable.merequester.requester.abstraction;

import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.network.RequesterSyncPacket;
import com.almostreliable.merequester.requester.Request;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.networking.IGrid;
import appeng.api.stacks.GenericStack;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRequesterMenu extends AEBaseMenu {

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
        if (requestTracker == null || isInvalidRequestIndex(requestTracker, slot)) return;

        var requestSlot = requestTracker.getServer().getConfigInventory().getSlotInv(slot);
        var requestStack = requestSlot.getStackInSlot(0);
        var carriedStack = getCarried();

        // the screen only has fake slots, so don't transfer anything to the player's inventory
        switch (action) {
            case PICKUP_OR_SET_DOWN -> requestSlot.setItemDirect(0, carriedStack.isEmpty() ? ItemStack.EMPTY : carriedStack.copy());
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
                    requestSlot.insertItem(0, GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()), false);
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
        var request = getServerRequest(requesterId, requestIndex);
        if (request == null) return;

        request.updateState(state);
    }

    public void updateRequesterNumbers(long requesterId, int requestIndex, long amount, long batch) {
        var request = getServerRequest(requesterId, requestIndex);
        if (request == null) return;

        request.updateAmount(amount);
        request.updateBatch(batch);
    }

    @Nullable
    private Request getServerRequest(long requesterId, int requestIndex) {
        var requestTracker = getRequestTracker(requesterId);
        if (requestTracker == null) return null;

        if (isInvalidRequestIndex(requestTracker, requestIndex)) return null;

        return requestTracker.getServer().get(requestIndex);
    }

    protected abstract void sendFullUpdate(@Nullable IGrid grid);

    protected abstract void sendPartialUpdate();

    protected void syncRequestTrackerFull(RequestTracker requestTracker) {
        var server = requestTracker.getServer();
        var client = requestTracker.getClient();

        var updates = new ArrayList<RequesterSyncPacket.IndexedRequest>(server.size());
        for (var i = 0; i < server.size(); i++) {
            var component = server.get(i).toComponent();
            client.get(i).fromComponent(component);
            updates.add(new RequesterSyncPacket.IndexedRequest(i, component));
        }

        sendInventorySync(requestTracker, updates);
    }

    protected void syncRequestTrackerPartial(RequestTracker requestTracker) {
        var server = requestTracker.getServer();
        var client = requestTracker.getClient();

        List<RequesterSyncPacket.IndexedRequest> updates = null;
        for (var i = 0; i < server.size(); i++) {
            var serverRequest = server.get(i);
            var clientRequest = client.get(i);

            if (serverRequest.isDifferent(clientRequest)) {
                if (updates == null) {
                    updates = new ArrayList<>();
                }

                var component = serverRequest.toComponent();
                clientRequest.fromComponent(component);
                updates.add(new RequesterSyncPacket.IndexedRequest(i, component));
            }
        }

        if (updates != null) {
            sendInventorySync(requestTracker, updates);
        }
    }

    private void sendInventorySync(RequestTracker requestTracker, List<RequesterSyncPacket.IndexedRequest> updates) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(
                serverPlayer,
                RequesterSyncPacket.createInventory(
                    requestTracker.getId(),
                    requestTracker.getName(),
                    requestTracker.getSortBy(),
                    requestTracker.getServer().size(),
                    updates
                )
            );
        }
    }

    private boolean isInvalidRequestIndex(RequestTracker requestTracker, int index) {
        if (requestTracker.getServer().isInvalidIndex(index)) {
            MERequester.LOGGER.warn("Requester Screen refers to invalid slot {} of {}", index, requestTracker.getName());
            return true;
        }
        return false;
    }

    protected RequestTracker createTracker(RequesterBlockEntity requester) {
        RequestTracker requestTracker = new RequestTracker(requester, idSerial);
        idSerial++;
        return requestTracker;
    }

    @Nullable
    protected abstract RequestTracker getRequestTracker(long id);
}
