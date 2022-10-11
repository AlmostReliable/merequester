package com.almostreliable.merequester.terminal;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.GenericStack;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.me.interaction.StackInteractions;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.network.RequesterTerminalPacket;
import com.almostreliable.merequester.network.ServerToClientPacket;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.Requests;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * yoinked from {@link PatternAccessTermMenu}
 */
public final class RequesterTerminalMenu extends AEBaseMenu {

    public static final MenuType<RequesterTerminalMenu> TYPE = MenuTypeBuilder
        .create(RequesterTerminalMenu::new, RequesterTerminalPart.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(MERequester.TERMINAL_ID);

    public static final String SORT_BY_ID = "sort_by";
    public static final String UNIQUE_NAME_ID = "unique_name";

    private final Long2ObjectOpenHashMap<RequestTracker> byId = new Long2ObjectOpenHashMap<>();
    private final Map<RequesterBlockEntity, RequestTracker> byRequester = new IdentityHashMap<>();

    // used to give requesters unique IDs
    private long idSerial = Long.MIN_VALUE;

    private RequesterTerminalMenu(int id, Inventory playerInventory, RequesterTerminalPart host) {
        super(TYPE, id, playerInventory, host);
        createPlayerInventorySlots(playerInventory);
    }

    @Override
    public void broadcastChanges() {
        if (isClientSide()) return;
        super.broadcastChanges();

        IGrid grid = getGrid();
        if (grid == null) return;

        VisitorState state = visitRequesters(grid);
        if (state.forceFullUpdate || state.total != byRequester.size()) {
            sendFullUpdate(grid);
        } else {
            sendPartialUpdate();
        }
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        RequestTracker requestTracker = byId.get(id);
        if (requestTracker == null) return;

        if (slot < 0 || slot >= requestTracker.server.size()) {
            MERequester.LOGGER.warn("Requester Terminal refers to invalid slot {} of {}", slot, requestTracker.name);
            return;
        }

        var requestSlot = requestTracker.server.getSlotInv(slot);
        var requestStack = requestSlot.getStackInSlot(0);
        var carriedStack = getCarried();

        // the screen only has fake slots, so don't transfer anything to the player's inventory
        switch (action) {
            case PICKUP_OR_SET_DOWN:
                requestSlot.setItemDirect(0, carriedStack.isEmpty() ? ItemStack.EMPTY : carriedStack.copy());
                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (carriedStack.isEmpty()) {
                    requestSlot.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    var copy = carriedStack.copy();
                    copy.setCount(1);
                    requestSlot.setItemDirect(0, copy);
                }
                break;
            case SHIFT_CLICK:
                requestSlot.setItemDirect(0, ItemStack.EMPTY);
                break;
            case EMPTY_ITEM:
                var emptyingAction = StackInteractions.getEmptyingAction(carriedStack);
                if (emptyingAction != null) {
                    requestSlot.insertItem(
                        0,
                        GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()),
                        false
                    );
                }
                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild && carriedStack.isEmpty()) {
                    if (requestStack.isEmpty()) {
                        setCarried(ItemStack.EMPTY);
                    } else {
                        var stack = requestStack.copy();
                        stack.setCount(stack.getMaxStackSize());
                        setCarried(stack);
                    }
                }
                break;
            default:
                MERequester.LOGGER.debug("Unsupported action {} in Requester Terminal", action);
        }
    }

    @Override
    protected ItemStack transferStackToMenu(ItemStack stack) {
        // sort the requesters like in the screen to refer to the same slots
        var requesters = byRequester.keySet()
            .stream().sorted(Comparator.comparingLong(RequesterBlockEntity::getSortValue)).toList();

        // find the first available slot and put the stack there
        for (var requester : requesters) {
            var targetSlot = requester.getRequests().firstAvailableIndex();
            if (targetSlot == -1) continue;
            byRequester.get(requester).server.insertItem(targetSlot, stack, false);
            return stack;
        }
        return stack;
    }

    private VisitorState visitRequesters(IGrid grid) {
        VisitorState state = new VisitorState();
        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            RequestTracker requestTracker = byRequester.get(requester);
            if (requestTracker == null || !requestTracker.name.equals(requester.getTerminalName().getString())) {
                state.forceFullUpdate = true;
                return state;
            }
            state.total++;
        }
        return state;
    }

    private void sendFullUpdate(IGrid grid) {
        byId.clear();
        byRequester.clear();

        // clear the current data on the client
        sendClientPacket(RequesterTerminalPacket.clearData());

        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            byRequester.put(requester, new RequestTracker(requester));
        }

        for (var requestTracker : byRequester.values()) {
            byId.put(requestTracker.id, requestTracker);

            var server = requestTracker.server;
            var client = requestTracker.client;

            // get the requests from the server
            var tag = server.serializeNBT();
            // store the information in the client tracker to
            // check for differences on partial updates later
            // tag serialization is used to avoid references to the original data
            client.deserializeNBT(tag);

            // send relevant data to the client
            tag.putString(UNIQUE_NAME_ID, requestTracker.name);
            tag.putLong(SORT_BY_ID, requestTracker.sortBy);
            sendClientPacket(RequesterTerminalPacket.inventory(requestTracker.id, tag));
        }
    }

    private void sendPartialUpdate() {
        for (var requestTracker : byRequester.values()) {
            var server = requestTracker.server;
            var client = requestTracker.client;

            CompoundTag tag = null;
            // iterate through the server data and check for differences
            for (var i = 0; i < server.size(); i++) {
                var serverRequest = server.get(i);
                var clientRequest = client.get(i);

                if (serverRequest.isDifferent(clientRequest)) {
                    // write initial data as soon as something is different
                    if (tag == null) {
                        tag = new CompoundTag();
                        tag.putString(UNIQUE_NAME_ID, requestTracker.name);
                        tag.putLong(SORT_BY_ID, requestTracker.sortBy);
                    }

                    var serverData = serverRequest.serializeNBT();
                    tag.put(String.valueOf(i), serverData);
                    // update the client information for future difference checks
                    clientRequest.deserializeNBT(serverData);
                }
            }

            // only send an update if something changed
            if (tag != null) {
                sendClientPacket(RequesterTerminalPacket.inventory(requestTracker.id, tag));
            }
        }
    }

    private void sendClientPacket(ServerToClientPacket<?> packet) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    public void updateRequesterState(long requesterId, int requestIndex, boolean state) {
        var request = byId.get(requesterId).server.get(requestIndex);
        request.updateState(state);
    }

    public void updateRequesterNumbers(long requesterId, int requestIndex, long amount, long batch) {
        var request = byId.get(requesterId).server.get(requestIndex);
        request.updateAmount(amount);
        request.updateBatch(batch);
    }

    @Nullable
    private IGrid getGrid() {
        IActionHost host = getActionHost();
        if (host != null) {
            IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                return agn.getGrid();
            }
        }
        return null;
    }

    private static class VisitorState {
        private int total;
        private boolean forceFullUpdate;
    }

    private final class RequestTracker {

        private final long id = idSerial++;
        private final long sortBy;
        private final String name;
        private final Requests server;
        private final Requests client;

        private RequestTracker(RequesterBlockEntity requester) {
            this.sortBy = requester.getSortValue();
            this.name = requester.getTerminalName().getString();
            this.server = requester.getRequests();
            this.client = new Requests();
        }
    }
}
