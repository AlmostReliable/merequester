package com.almostreliable.merequester.requester;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.MachineSource;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.RequestHost;
import com.almostreliable.merequester.requester.status.LinkState;
import com.almostreliable.merequester.requester.status.StatusState;
import com.almostreliable.merequester.requester.status.RequestStatus;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

public class RequesterBlockEntity extends AENetworkBlockEntity implements RequestHost, IGridTickable, ICraftingRequester {

    public static final int SIZE = 5;

    private final Requests requests;
    private final StatusState[] requestStatus;
    private final StorageManager storageManager;
    private final IActionSource actionSource;

    private TickRateModulation currentTickRate = TickRateModulation.IDLE;

    public RequesterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        requests = new Requests(this);
        requestStatus = new StatusState[SIZE];
        Arrays.fill(requestStatus, StatusState.IDLE);
        storageManager = new StorageManager(this);
        actionSource = new MachineSource(this);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL) // TODO: make configurable
            .addService(IGridTickable.class, this)
            .addService(ICraftingRequester.class, this)
            .addService(IStorageWatcherNode.class, storageManager)
            .setIdlePowerUsage(5) // TODO: make configurable
            .setExposedOnSides(getExposedSides());
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (level == null || level.isClientSide || !getMainNode().isActive()) return TickRateModulation.IDLE;
        if (handleRequests()) setChanged();
        return currentTickRate;
    }

    @Override
    public void requestChanged(int index) {
        storageManager.clear(index);
        saveChanges();
    }

    @Override
    public Requests getRequests() {
        return requests;
    }

    @Override
    public Component getTerminalName() {
        return hasCustomInventoryName() ?
            getCustomInventoryName() :
            Utils.translate("block", MERequester.REQUESTER_ID);
    }

    @Override
    public void setOrientation(Direction inForward, Direction inUp) {
        super.setOrientation(inForward, inUp);
        getMainNode().setExposedOnSides(getExposedSides());
    }

    private boolean handleRequests() {
        var changed = false;

        var tickRateModulation = TickRateModulation.IDLE;
        for (var slot = 0; slot < requestStatus.length; slot++) {
            var state = requestStatus[slot];
            var result = handleRequest(slot);
            if (!Objects.equals(state, result)) {
                changed = true;
            }
            var resultTickRateModulation = result.getTickRateModulation();
            if (resultTickRateModulation.ordinal() > tickRateModulation.ordinal()) {
                tickRateModulation = resultTickRateModulation;
            }

            updateRequestStatus(slot, result);
        }
        currentTickRate = tickRateModulation;
        return changed;
    }

    private StatusState handleRequest(int slot) {
        var state = requestStatus[slot];
        updateRequestStatus(slot, state.handle(this, slot));
        if (requestStatus[slot].type() != RequestStatus.IDLE && !Objects.equals(requestStatus[slot], state)) {
            return handleRequest(slot);
        }

        return requestStatus[slot];
    }

    private void updateRequestStatus(int slot, StatusState state) {
        requestStatus[slot] = state;
        requests.get(slot).setClientStatus(state.type());
        markForUpdate();
    }

    private EnumSet<Direction> getExposedSides() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(getForward());
        return exposedSides;
    }

    boolean isActive() {
        return Arrays.stream(requestStatus).anyMatch(p -> p.type().translateToClient() != RequestStatus.IDLE);
    }

    public IGrid getMainNodeGrid() {
        var grid = getMainNode().getGrid();
        Objects.requireNonNull(grid, "RequesterBlockEntity was not fully initialized - Grid is null");
        return grid;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public IActionSource getActionSource() {
        return actionSource;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return Arrays.stream(requestStatus)
            .filter(LinkState.class::isInstance)
            .map(state -> ((LinkState) state).link())
            .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        for (var slot = 0; slot < requestStatus.length; slot++) {
            var state = requestStatus[slot];
            if (state instanceof LinkState cls && cls.link().equals(link)) {
                if (!mode.isSimulate()) storageManager.get(slot).update(what, amount);
                return amount;
            }
        }
        throw new IllegalStateException("No CraftingLinkState found");
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        // tracked by request status
    }

    public long getSortValue() {
        return (long) worldPosition.getZ() << 24 ^ (long) worldPosition.getX() << 8 ^ worldPosition.getY();
    }
}
