package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.core.Config;
import com.almostreliable.merequester.core.ModRegistration;
import com.almostreliable.merequester.requester.abstraction.RequestHost;
import com.almostreliable.merequester.requester.status.BlockingState;
import com.almostreliable.merequester.requester.status.LinkState;
import com.almostreliable.merequester.requester.status.RequestStatus;
import com.almostreliable.merequester.requester.status.StatusState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import appeng.util.SettingsFrom;
import com.google.common.collect.ImmutableSet;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RequesterBlockEntity extends AENetworkedBlockEntity implements RequestHost, IGridTickable, ICraftingRequester {

    // serialization IDs
    private static final String REQUESTS_ID = "requests";
    private static final String REQUEST_STATUS_ID = "request_status";
    private static final String STORAGE_MANAGER_ID = "storage_manager";

    private final RequestManager requestManager;
    private final StatusState[] requestStatus;
    private final StorageManager storageManager;
    private final IActionSource actionSource;

    private TickRateModulation currentTickRate = TickRateModulation.IDLE;

    public RequesterBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModRegistration.REQUESTER_ENTITY.get(), pos, blockState);
    }

    public RequesterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        var possibleRequests = Config.COMMON.requests.get();
        requestManager = new RequestManager(this, possibleRequests);
        requestStatus = new StatusState[possibleRequests];
        Arrays.fill(requestStatus, StatusState.IDLE);
        storageManager = new StorageManager(this, possibleRequests);
        actionSource = new MachineSource(this);
        getMainNode()
            .setExposedOnSides(getExposedSides())
            .addService(IGridTickable.class, this)
            .addService(ICraftingRequester.class, this)
            .addService(IStorageWatcherNode.class, storageManager)
            .setIdlePowerUsage(Config.COMMON.idleEnergy.get());
        if (Config.COMMON.requireChannel.get()) {
            getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        }
    }

    @Override
    public void loadTag(ValueInput data) {
        super.loadTag(data);
        requestManager.deserialize(data.childOrEmpty(REQUESTS_ID));
        deserializeStatus(data.childOrEmpty(REQUEST_STATUS_ID));
        storageManager.deserialize(data.childOrEmpty(STORAGE_MANAGER_ID));
    }

    @Override
    public void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        requestManager.serialize(data.child(REQUESTS_ID));
        serializeStatus(data.child(REQUEST_STATUS_ID));
        storageManager.serialize(data.child(STORAGE_MANAGER_ID));
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            var exportedRequests = input.get(ModRegistration.EXPORTED_REQUESTS.get());
            if (exportedRequests != null) {
                requestManager.fromComponent(exportedRequests);
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(ModRegistration.EXPORTED_REQUESTS.get(), requestManager.toComponent());
        }
    }

    @Override
    public void onReady() {
        getMainNode().setExposedOnSides(getExposedSides());
        super.onReady();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (level == null || level.isClientSide() || !getMainNode().isActive()) return TickRateModulation.IDLE;
        if (handleRequests()) setChanged();
        return currentTickRate;
    }

    @Override
    public void requestChanged(int index) {
        if (requestStatus[index] instanceof BlockingState) {
            updateRequestStatus(index, StatusState.IDLE);
        }

        saveChanges();
    }

    @Override
    public void requestKeyChanged(int index) {
        storageManager.clear(index);
        requestChanged(index);
    }

    @Override
    public RequestManager getRequestManager() {
        return requestManager;
    }

    @Override
    public Component getTerminalName() {
        return hasCustomName() ? Objects.requireNonNull(getCustomName()) : Utils.translate("block", MERequester.REQUESTER_ID);
    }

    @Override
    public void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        getMainNode().setExposedOnSides(getExposedSides());
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return getExposedSides();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        storageManager.addDrops(drops);
    }

    private void deserializeStatus(ValueInput data) {
        for (var i = 0; i < requestStatus.length; i++) {
            var child = data.child(String.valueOf(i));
            if (child.isPresent()) {
                var link = StorageHelper.loadCraftingLink(child.orElseThrow(), this);
                requestStatus[i] = new LinkState(link);
            }
        }
    }

    private void serializeStatus(ValueOutput data) {
        for (var i = 0; i < requestStatus.length; i++) {
            var state = requestStatus[i];
            if (state instanceof LinkState cls) {
                cls.link().writeToNBT(data.child(String.valueOf(i)));
            }
        }
    }

    private boolean handleRequests() {
        var changed = false;

        var tickRateModulation = TickRateModulation.IDLE;
        for (var i = 0; i < requestStatus.length; i++) {
            var state = requestStatus[i];
            var result = handleRequest(i);
            if (!Objects.equals(state, result)) {
                changed = true;
            }
            var resultTickRateModulation = result.getTickRateModulation();
            if (resultTickRateModulation.ordinal() > tickRateModulation.ordinal()) {
                tickRateModulation = resultTickRateModulation;
            }

            updateRequestStatus(i, result);
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
        if (state.type().translateToClient() != requestManager.get(slot).getClientStatus()) {
            requestManager.get(slot).setClientStatus(state.type().translateToClient());
            markForUpdate();
        }
    }

    private EnumSet<Direction> getExposedSides() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(getFront());
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
        return Arrays
            .stream(requestStatus)
            .filter(LinkState.class::isInstance)
            .map(state -> ((LinkState) state).link())
            .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        for (var i = 0; i < requestStatus.length; i++) {
            var state = requestStatus[i];
            if (state instanceof LinkState cls && cls.link().equals(link)) {
                if (mode != Actionable.SIMULATE) storageManager.get(i).update(what, amount);
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
