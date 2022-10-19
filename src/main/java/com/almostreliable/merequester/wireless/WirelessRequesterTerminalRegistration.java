package com.almostreliable.merequester.wireless;

import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.WirelessTerminalItem;
import com.almostreliable.merequester.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class WirelessRequesterTerminalRegistration {
    public static WirelessTerminalItem WIRELESS_REQUESTER_TERMINAL;

    public static void registerWirelessRequesterTerminal() {
        WIRELESS_REQUESTER_TERMINAL = new WirelessRequesterTerminalItem();
        Registry.register(Registry.ITEM, new ResourceLocation("merequester", "wireless_requester_terminal"), WIRELESS_REQUESTER_TERMINAL);
        GridLinkables.register(WIRELESS_REQUESTER_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
        Registry.register(Registry.MENU, Utils.getRL(WirelessRequesterTerminalMenu.ID), WirelessRequesterTerminalMenu.TYPE);
        Upgrades.add(AEItems.ENERGY_CARD, WIRELESS_REQUESTER_TERMINAL, 2, GuiText.WirelessTerminals.getTranslationKey());
    }
}
