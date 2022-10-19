package com.almostreliable.merequester.wireless;

import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.ISubMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class WirelessRequesterTerminalMenuHost extends WirelessTerminalMenuHost {
    public WirelessRequesterTerminalMenuHost(
        Player player, @Nullable Integer slot, ItemStack itemStack, BiConsumer<Player, ISubMenu> returnToMainMenu
    ) {
        super(player, slot, itemStack, returnToMainMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(WirelessRequesterTerminalRegistration.WIRELESS_REQUESTER_TERMINAL);
    }
}
