package com.almostreliable.merequester.wireless.ae2wtlib;

import de.mari_023.ae2wtlib.terminal.ItemWT;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ItemWR extends ItemWT {
    @Override
    public MenuType<?> getMenuType(ItemStack stack) {
        return WRMenu.TYPE;
    }
}
