package com.almostreliable.merequester.wireless;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AEConfig;
import appeng.items.tools.powered.WirelessTerminalItem;
import com.almostreliable.merequester.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WirelessRequesterTerminalItem extends WirelessTerminalItem {
    public WirelessRequesterTerminalItem() {
        super(
            AEConfig.instance().getWirelessTerminalBattery(),
            new Item.Properties().tab(Registration.TAB).stacksTo(1)
        );
    }

    @Nullable
    public ItemMenuHost getMenuHost(Player player, int slot, ItemStack stack, @Nullable BlockPos pos) {
        return new WirelessRequesterTerminalMenuHost(player, slot, stack, (p, subMenu) -> this.openFromInventory(p, slot, true));
    }

    public MenuType<?> getMenuType() {
        return WirelessRequesterTerminalMenu.TYPE;
    }
}
