package com.almostreliable.merequester.wireless.ae2wtlib;

import appeng.menu.ISubMenu;
import com.almostreliable.merequester.wireless.WirelessRequesterTerminalRegistration;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class WRMenuHost extends WTMenuHost {
    public WRMenuHost(Player player, @Nullable Integer inventorySlot, ItemStack is, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, inventorySlot, is, returnToMainMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(WirelessRequesterTerminalRegistration.WIRELESS_REQUESTER_TERMINAL);
    }
}
