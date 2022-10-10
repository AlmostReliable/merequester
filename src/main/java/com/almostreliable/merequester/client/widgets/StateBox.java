package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import com.almostreliable.merequester.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public class StateBox extends AECheckbox {

    private static final int SIZE = 14;

    private static final Blitter BLITTER = Blitter.texture(
        Utils.getRL("textures/gui/state_box.png"), SIZE * 2, SIZE * 2
    );
    private static final Blitter UNCHECKED = BLITTER.copy().src(0, 0, SIZE, SIZE);
    private static final Blitter UNCHECKED_FOCUS = BLITTER.copy().src(SIZE, 0, SIZE, SIZE);
    private static final Blitter CHECKED = BLITTER.copy().src(0, SIZE, SIZE, SIZE);
    private static final Blitter CHECKED_FOCUS = BLITTER.copy().src(SIZE, SIZE, SIZE, SIZE);

    StateBox(int x, int y, ScreenStyle style) {
        // add 2 to the positions to it matches with a slot
        super(x + 2, y + 2, SIZE, SIZE, style, Component.empty());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mX, int mY, float partial) {
        Blitter icon;
        if (isFocused() || isMouseOver(mX, mY)) {
            icon = isSelected() ? CHECKED_FOCUS : UNCHECKED_FOCUS;
        } else {
            icon = isSelected() ? CHECKED : UNCHECKED;
        }
        var opacity = isActive() ? 1 : 0.5f;
        icon.dest(x, y).opacity(opacity).blit(poseStack, getBlitOffset());
    }
}
