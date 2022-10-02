package com.almostreliable.merequester.requester;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.TypeFilter;
import appeng.api.util.IConfigurableObject;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.almostreliable.merequester.MERequester;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class RequesterTerminalMenu extends AEBaseMenu {

    public static final MenuType<RequesterTerminalMenu> TYPE = MenuTypeBuilder
        .create(RequesterTerminalMenu::new, RequesterTerminal.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(MERequester.TERMINAL_ID);

    private final IConfigurableObject host;
    @GuiSync(1) public TypeFilter typeFilter = TypeFilter.ALL; // test if this needs to be public for the gui sync annotation

    public RequesterTerminalMenu(int id, Inventory playerInventory, RequesterTerminal host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        createPlayerInventorySlots(playerInventory);
    }

    public TypeFilter getTypeFilter() {
        return typeFilter;
    }
}
