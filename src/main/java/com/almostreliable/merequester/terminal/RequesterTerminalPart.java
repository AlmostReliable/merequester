package com.almostreliable.merequester.terminal;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPartItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.parts.reporting.PatternAccessTerminalPart;

/**
 * yoinked from {@link PatternAccessTerminalPart}
 */
public class RequesterTerminalPart extends AbstractDisplayPart implements RequesterTerminalHost {

    public RequesterTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!super.onUseWithoutItem(player, pos) && !isClientSide()) {
            MenuOpener.open(RequesterTerminalMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }
}
