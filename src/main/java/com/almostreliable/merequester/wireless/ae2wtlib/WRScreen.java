package com.almostreliable.merequester.wireless.ae2wtlib;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.BackgroundPanel;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.menu.SlotSemantics;
import com.almostreliable.merequester.client.RequesterTerminalScreen;
import de.mari_023.ae2wtlib.wut.CycleTerminalButton;
import de.mari_023.ae2wtlib.wut.IUniversalTerminalCapable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WRScreen extends RequesterTerminalScreen<WRMenu> implements IUniversalTerminalCapable {
    public WRScreen(WRMenu menu, Inventory playerInventory, Component name, ScreenStyle style) {
        super(menu, playerInventory, name, style);
        if (getMenu().isWUT())
            addToLeftToolbar(new CycleTerminalButton(btn -> cycleTerminal()));

        widgets.add("upgrades", new UpgradesPanel(getMenu().getSlots(SlotSemantics.UPGRADE), getMenu().getHost()));
        if (getMenu().getToolbox().isPresent())
            widgets.add("toolbox", new ToolboxPanel(style, getMenu().getToolbox().getName()));
        widgets.add("singularityBackground", new BackgroundPanel(style.getImage("singularityBackground")));
    }

    @Override
    public void storeState() {}
}
