package com.almostreliable.merequester.wireless.ae2wtlib;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import de.mari_023.ae2wtlib.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.wct.WCTMenuHost;
import de.mari_023.ae2wtlib.wut.ItemWUT;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class WRMenu extends RequesterTerminalMenu {
    public static final String ID = "wrt";

    public static final MenuType<WRMenu> TYPE = MenuTypeBuilder
        .create(WRMenu::new, WRMenuHost.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(ID);

    private final WRMenuHost wrMenuHost;
    private final ToolboxMenu toolboxMenu;

    public WRMenu(int id, Inventory playerInventory, WRMenuHost host) {
        super(TYPE, id, playerInventory, host);
        wrMenuHost = host;
        toolboxMenu = new ToolboxMenu(this);

        IUpgradeInventory upgrades = wrMenuHost.getUpgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, i);
            slot.setNotDraggable();
            addSlot(slot, SlotSemantics.UPGRADE);
        }
        addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY, wrMenuHost.getSubInventory(
            WCTMenuHost.INV_SINGULARITY), 0), AE2wtlibSlotSemantics.SINGULARITY);

    }

    public boolean isWUT() {
        return wrMenuHost.getItemStack().getItem() instanceof ItemWUT;
    }

    public ITerminalHost getHost() {
        return wrMenuHost;
    }

    public ToolboxMenu getToolbox() {
        return toolboxMenu;
    }
}
