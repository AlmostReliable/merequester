package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import com.almostreliable.merequester.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public class SubmitButton extends AECheckbox {

    private static final int SIZE = 12;

    private static final Blitter BLITTER = Blitter.texture(
        Utils.getRL("textures/gui/submit_button.png"), SIZE * 2, SIZE
    );
    private static final Blitter UNFOCUSED = BLITTER.copy().src(0, 0, SIZE, SIZE);
    private static final Blitter FOCUSED = BLITTER.copy().src(SIZE, SIZE, SIZE, SIZE);

    SubmitButton(int x, int y, ScreenStyle style) {
        super(x, y, SIZE, SIZE, style, Component.empty());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mX, int mY, float partial) {
        Blitter icon = isFocused() || isMouseOver(mX, mY) ? FOCUSED : UNFOCUSED;
        var opacity = isActive() ? 1 : 0.5f;
        icon.dest(x, y).opacity(opacity).blit(poseStack, getBlitOffset());
    }
}
