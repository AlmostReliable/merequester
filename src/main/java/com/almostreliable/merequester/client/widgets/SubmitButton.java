package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.ITooltip;
import com.almostreliable.merequester.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import java.util.List;

public class SubmitButton extends AECheckbox implements ITooltip {

    private static final int SIZE = 12;

    private static final Blitter BLITTER = Blitter.texture(
        Utils.getRL("textures/gui/submit_button.png"), SIZE * 2, SIZE
    );
    private static final Blitter UNFOCUSED = BLITTER.copy().src(0, 0, SIZE, SIZE);
    private static final Blitter FOCUSED = BLITTER.copy().src(SIZE, SIZE, SIZE, SIZE);

    SubmitButton(int x, int y, ScreenStyle style, Runnable changeListener) {
        super(x, y, SIZE, SIZE, style, Component.empty());
        setChangeListener(changeListener);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mX, int mY, float partial) {
        Blitter icon = isFocused() || isMouseOver(mX, mY) ? FOCUSED : UNFOCUSED;
        var opacity = isActive() ? 1 : 0.5f;
        icon.dest(getX(), getY()).opacity(opacity).blit(guiGraphics);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(Utils.translate("tooltip", "submit"));
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), width, height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }
}
