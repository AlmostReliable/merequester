package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.network.RequesterSyncPacket;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import com.almostreliable.merequester.requester.abstraction.RequestTracker;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.networking.IGrid;
import appeng.menu.implementations.MenuTypeBuilder;

import org.jetbrains.annotations.Nullable;

public final class RequesterMenu extends AbstractRequesterMenu {

    public static final MenuType<RequesterMenu> TYPE = MenuTypeBuilder
        .create(RequesterMenu::new, RequesterBlockEntity.class)
        .withInitialData(
            (host, buffer) -> buffer.writeVarInt(host.getRequestManager().size()),
            (host, menu, buffer) -> menu.requestCount = buffer.readVarInt()
        )
        .buildUnregistered(Utils.getRL(MERequester.REQUESTER_ID));

    private int requestCount;
    @Nullable
    private RequestTracker requestTracker;

    private RequesterMenu(int id, Inventory playerInventory, RequesterBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        requestCount = host.getRequestManager().size();
    }

    @Override
    public void broadcastChanges() {
        if (isClientSide()) return;
        super.broadcastChanges();
        if (requestTracker == null) {
            sendFullUpdate(null);
        } else {
            sendPartialUpdate();
        }
    }

    @Override
    protected int transferStackToMenu(ItemStack stack) {
        assert requestTracker != null;
        var firstAvailable = requestTracker.getServer().firstAvailableIndex();
        if (firstAvailable != -1) {
            requestTracker.getServer().insertItem(firstAvailable, stack, false);
        }
        return 0;
    }

    @Override
    protected void sendFullUpdate(@Nullable IGrid grid) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, RequesterSyncPacket.createClearData());
        }
        requestTracker = createTracker((RequesterBlockEntity) getBlockEntity());
        syncRequestTrackerFull(requestTracker);
    }

    @Override
    protected void sendPartialUpdate() {
        assert requestTracker != null;
        syncRequestTrackerPartial(requestTracker);
    }

    @Nullable
    @Override
    protected RequestTracker getRequestTracker(long id) {
        return requestTracker;
    }

    public int getRequestCount() {
        return requestCount;
    }
}
