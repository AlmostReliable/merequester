package com.almostreliable.merequester.requester;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
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
import appeng.util.inv.InternalInventoryHost;
import com.almostreliable.merequester.requester.progression.CraftingLinkState;
import com.almostreliable.merequester.requester.progression.IProgressionState;
import com.almostreliable.merequester.requester.progression.ProgressionType;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

public class RequesterBlockEntity extends AENetworkBlockEntity implements InternalInventoryHost, IGridTickable, ICraftingRequester {

    public static final int SLOTS = 5; // TODO: make configurable

    private final Requests requests;
    private final IProgressionState[] progressions;
    private final StorageManager storageManager;
    private final IActionSource actionSource;

    private TickRateModulation currentTickRate = TickRateModulation.IDLE;

    public RequesterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        requests = new Requests(this);
        progressions = new IProgressionState[SLOTS];
        Arrays.fill(progressions, IProgressionState.IDLE);
        storageManager = new StorageManager(this);
        actionSource = new MachineSource(this);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL) // TODO: make configurable
            .addService(IGridTickable.class, this)
            .addService(ICraftingRequester.class, this)
            .addService(IStorageWatcherNode.class, storageManager)
            .setIdlePowerUsage(5) // TODO: make configurable
            .setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (level == null || level.isClientSide || !getMainNode().isActive()) return TickRateModulation.IDLE;
        if (handleProgression()) setChanged();
        return currentTickRate;
    }

    public Requests getRequests() {
        return requests;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        storageManager.clear(slot);
        saveChanges();
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
        return Arrays.stream(progressions)
            .filter(CraftingLinkState.class::isInstance)
            .map(state -> ((CraftingLinkState) state).link())
            .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        for (var slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            if (state instanceof CraftingLinkState cls && cls.link().equals(link)) {
                if (!mode.isSimulate()) storageManager.get(slot).update(what, amount);
                return amount;
            }
        }
        throw new IllegalStateException("No CraftingLinkState found");
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        // handled by progression states
    }

    private boolean handleProgression() {
        var changed = false;

        var tickRateModulation = TickRateModulation.IDLE;
        for (var slot = 0; slot < progressions.length; slot++) {
            var state = progressions[slot];
            var result = handleProgression(slot);
            if (!Objects.equals(state, result)) {
                changed = true;
            }
            var resultTickRateModulation = result.getTickRateModulation();
            if (resultTickRateModulation.ordinal() > tickRateModulation.ordinal()) {
                tickRateModulation = resultTickRateModulation;
            }

            progressions[slot] = result;
        }
        currentTickRate = tickRateModulation;
        return changed;
    }

    private IProgressionState handleProgression(int slot) {
        var state = progressions[slot];
        progressions[slot] = state.handle(this, slot);
        if (progressions[slot].type() != ProgressionType.IDLE && !Objects.equals(progressions[slot], state)) {
            return handleProgression(slot);
        }

        return progressions[slot];
    }
}
