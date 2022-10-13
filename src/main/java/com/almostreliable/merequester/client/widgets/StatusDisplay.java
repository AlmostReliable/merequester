package com.almostreliable.merequester.client.widgets;

import appeng.client.gui.widgets.ITooltip;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.status.RequestStatus;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.almostreliable.merequester.Utils.f;

public class StatusDisplay extends AbstractWidget implements ITooltip {

    private static final int WIDTH = 118;
    private static final int HEIGHT = 2;

    private final BooleanSupplier isInactive;

    private RequestStatus status = RequestStatus.IDLE;

    StatusDisplay(int x, int y, BooleanSupplier isInactive) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.isInactive = isInactive;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mX, int mY, float partialTick) {
        fill(poseStack, x, y, x + width, y + height, Utils.fillColorAlpha(getStatusColor()));
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    private ChatFormatting getStatusColor(RequestStatus requestStatus) {
        return switch (requestStatus) {
            case IDLE -> ChatFormatting.DARK_GREEN;
            case LINK -> ChatFormatting.YELLOW;
            case EXPORT -> ChatFormatting.DARK_PURPLE;
            default -> throw new IllegalStateException("Impossible client state: " + requestStatus);
        };
    }

    @Override
    public List<Component> getTooltipMessage() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Utils.translate("tooltip", "status"));
        if (Screen.hasShiftDown()) {
            tooltip.addAll(List.of(
                Component.literal(" "),
                Utils.translate("tooltip", RequestStatus.IDLE.toString().toLowerCase())
                    .withStyle(getStatusColor(RequestStatus.IDLE)),
                Utils.translate("tooltip", f("{}_desc", RequestStatus.IDLE.toString().toLowerCase())),
                Component.literal(" "),
                Utils.translate("tooltip", RequestStatus.LINK.toString().toLowerCase())
                    .withStyle(getStatusColor(RequestStatus.LINK)),
                Utils.translate("tooltip", f("{}_desc", RequestStatus.LINK.toString().toLowerCase())),
                Component.literal(" "),
                Utils.translate("tooltip", RequestStatus.EXPORT.toString().toLowerCase())
                    .withStyle(getStatusColor(RequestStatus.EXPORT)),
                Utils.translate("tooltip", f("{}_desc", RequestStatus.EXPORT.toString().toLowerCase()))
            ));
        } else {
            tooltip.add(Component.literal("» ").withStyle(ChatFormatting.AQUA)
                .append(Utils.translate(
                    "tooltip",
                    "shift_for_more",
                    InputConstants.getKey("key.keyboard.left.shift").getDisplayName()
                ).withStyle(ChatFormatting.GRAY)));
        }
        return tooltip;
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(x, y, width, height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    private ChatFormatting getStatusColor() {
        if (isInactive.getAsBoolean()) return ChatFormatting.DARK_GRAY;
        return getStatusColor(status);
    }

    void setStatus(RequestStatus status) {
        this.status = status;
    }
}
