package com.almostreliable.merequester.wireless;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class WirelessRequesterTerminalMenu extends RequesterTerminalMenu {
    public static final String ID = "wireless_requester_terminal";

    public static final MenuType<WirelessRequesterTerminalMenu> TYPE = MenuTypeBuilder
        .create(WirelessRequesterTerminalMenu::new, WirelessRequesterTerminalMenuHost.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(ID);

    private final WirelessRequesterTerminalMenuHost wrMenuHost;

    private final ToolboxMenu toolboxMenu;

    protected WirelessRequesterTerminalMenu(MenuType<?> menuType, int id, Inventory playerInventory, WirelessRequesterTerminalMenuHost host) {
        super(menuType, id, playerInventory, host);
        wrMenuHost = host;
        toolboxMenu = new ToolboxMenu(this);

        IUpgradeInventory upgrades = wrMenuHost.getUpgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, i);
            slot.setNotDraggable();
            addSlot(slot, SlotSemantics.UPGRADE);
        }
    }

    public ITerminalHost getHost() {
        return wrMenuHost;
    }

    public ToolboxMenu getToolbox() {
        return toolboxMenu;
    }
}
