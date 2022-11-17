package com.almostreliable.merequester.terminal;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.parts.reporting.PatternAccessTerminalPart;
import com.almostreliable.merequester.MERequester;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.almostreliable.merequester.Utils.f;

/**
 * yoinked from {@link PatternAccessTerminalPart}
 */
public class RequesterTerminalPart extends AbstractDisplayPart {

    @PartModels private static final ResourceLocation MODEL_OFF = AppEng.makeId(
        f("part/{}_off", MERequester.TERMINAL_ID)
    );
    @PartModels private static final ResourceLocation MODEL_ON = AppEng.makeId(
        f("part/{}_on", MERequester.TERMINAL_ID)
    );

    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    private static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public RequesterTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!super.onPartActivate(player, hand, pos) && !isClientSide()) {
            MenuOpener.open(RequesterTerminalMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
