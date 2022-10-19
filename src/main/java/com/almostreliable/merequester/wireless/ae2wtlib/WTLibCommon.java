package com.almostreliable.merequester.wireless.ae2wtlib;

import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.WirelessTerminalItem;
import com.almostreliable.merequester.wireless.WirelessRequesterTerminalRegistration;
import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.IWTLibAddonEntrypoint;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class WTLibCommon implements IWTLibAddonEntrypoint {
    @Override
    public void onWTLibInitialized() {
        ItemWR itemWR = new ItemWR();
        WirelessRequesterTerminalRegistration.WIRELESS_REQUESTER_TERMINAL = itemWR;
        Registry.register(Registry.ITEM, new ResourceLocation("merequester", "wireless_requester_terminal"), itemWR);
        GridLinkables.register(itemWR, WirelessTerminalItem.LINKABLE_HANDLER);
        WUTHandler.addTerminal("requester", itemWR::tryOpen, WRMenuHost::new, WRMenu.TYPE, itemWR);
        Registry.register(Registry.MENU, AppEng.makeId(WRMenu.ID), WRMenu.TYPE);

        var terminals = GuiText.WirelessTerminals.getTranslationKey();
        Upgrades.add(AE2wtlib.QUANTUM_BRIDGE_CARD, itemWR, 1, terminals);
    }
}
