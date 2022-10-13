package com.almostreliable.merequester.terminal;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.PatternAccessTermMenu;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.network.RequesterSyncPacket;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import com.almostreliable.merequester.requester.abstraction.RequestTracker;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * yoinked from {@link PatternAccessTermMenu}
 */
public final class RequesterTerminalMenu extends AbstractRequesterMenu {

    public static final MenuType<RequesterTerminalMenu> TYPE = MenuTypeBuilder
        .create(RequesterTerminalMenu::new, RequesterTerminalPart.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(MERequester.TERMINAL_ID);

    private final Long2ObjectOpenHashMap<RequestTracker> byId = new Long2ObjectOpenHashMap<>();
    private final Map<RequesterBlockEntity, RequestTracker> byRequester = new IdentityHashMap<>();

    private RequesterTerminalMenu(int id, Inventory playerInventory, RequesterTerminalPart host) {
        super(TYPE, id, playerInventory, host);
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
    protected ItemStack transferStackToMenu(ItemStack stack) {
        // sort the requesters like in the terminal to refer to the same slots
        var requesters = byRequester.keySet()
            .stream().sorted(Comparator.comparingLong(RequesterBlockEntity::getSortValue)).toList();

        // find the first available slot and put the stack there
        for (var requester : requesters) {
            var targetSlot = requester.getRequests().firstAvailableIndex();
            if (targetSlot == -1) continue;
            byRequester.get(requester).getServer().insertItem(targetSlot, stack, false);
            return stack;
        }
        return stack;
    }

    @Override
    protected void sendFullUpdate(@Nullable IGrid grid) {
        assert grid != null;
        byId.clear();
        byRequester.clear();

        // clear the current data on the client
        sendClientPacket(RequesterSyncPacket.clearData());

        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            byRequester.put(requester, createTracker(requester));
        }

        for (var requestTracker : byRequester.values()) {
            byId.put(requestTracker.getId(), requestTracker);
            syncRequestTrackerFull(requestTracker);
        }
    }

    @Override
    protected void sendPartialUpdate() {
        for (var requestTracker : byRequester.values()) {
            syncRequestTrackerPartial(requestTracker);
        }
    }

    @Nullable
    @Override
    protected RequestTracker getRequestTracker(long id) {
        return byId.get(id);
    }

    private VisitorState visitRequesters(IGrid grid) {
        VisitorState state = new VisitorState();
        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            RequestTracker requestTracker = byRequester.get(requester);
            if (requestTracker == null || !requestTracker.getName().equals(requester.getTerminalName().getString())) {
                state.forceFullUpdate = true;
                return state;
            }
            state.total++;
        }
        return state;
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
}
