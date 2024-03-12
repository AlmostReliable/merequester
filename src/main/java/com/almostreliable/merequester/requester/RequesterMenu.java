package com.almostreliable.merequester.requester;

import appeng.api.networking.IGrid;
import appeng.menu.implementations.MenuTypeBuilder;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import com.almostreliable.merequester.requester.abstraction.RequestTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public final class RequesterMenu extends AbstractRequesterMenu {

    public static final MenuType<RequesterMenu> TYPE = MenuTypeBuilder
        .create(RequesterMenu::new, RequesterBlockEntity.class)
        .build(MERequester.REQUESTER_ID);

    @Nullable private RequestTracker requestTracker;

    private RequesterMenu(int id, Inventory playerInventory, RequesterBlockEntity host) {
        super(TYPE, id, playerInventory, host);
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
    protected ItemStack transferStackToMenu(ItemStack stack) {
        assert requestTracker != null;
        var firstAvailable = requestTracker.getServer().firstAvailableIndex();
        if (firstAvailable != -1) {
            requestTracker.getServer().insertItem(firstAvailable, stack, false);
        }
        return stack;
    }

    @Override
    protected void sendFullUpdate(@Nullable IGrid grid) {
        Platform.sendClearData(getPlayer());
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
}
