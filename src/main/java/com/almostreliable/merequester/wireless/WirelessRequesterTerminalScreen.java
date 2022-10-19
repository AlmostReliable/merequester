package com.almostreliable.merequester.wireless;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.menu.SlotSemantics;
import com.almostreliable.merequester.client.RequesterTerminalScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WirelessRequesterTerminalScreen extends RequesterTerminalScreen<WirelessRequesterTerminalMenu> {
    public WirelessRequesterTerminalScreen(
        WirelessRequesterTerminalMenu menu, Inventory playerInventory, Component name, ScreenStyle style
    ) {
        super(menu, playerInventory, name, style);
        widgets.add("upgrades", new UpgradesPanel(getMenu().getSlots(SlotSemantics.UPGRADE), getMenu().getHost()));
        if (getMenu().getToolbox().isPresent())
            widgets.add("toolbox", new ToolboxPanel(style, getMenu().getToolbox().getName()));
    }
}
