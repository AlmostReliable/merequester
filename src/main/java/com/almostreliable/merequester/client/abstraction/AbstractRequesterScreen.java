package com.almostreliable.merequester.client.abstraction;

import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.RequestSlot;
import com.almostreliable.merequester.client.widgets.RequestWidget;
import com.almostreliable.merequester.mixin.accessors.WidgetContainerMixin;
import com.almostreliable.merequester.network.RequesterSyncPacket;
import com.almostreliable.merequester.requester.Request;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import com.mojang.blaze3d.platform.InputConstants;

import org.jetbrains.annotations.MustBeInvokedByOverriders;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * yoinked from {@link PatternAccessTermScreen}
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractRequesterScreen<M extends AbstractRequesterMenu> extends AEBaseScreen<M> implements RequestDisplay {

    protected static final int GUI_WIDTH = 195;
    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;
    protected static final int GUI_HEADER_HEIGHT = 20;
    protected static final int GUI_FOOTER_HEIGHT = 99;

    private static final int TEXT_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 156;

    protected static final int ROW_HEIGHT = 19;
    protected static final int MIN_ROW_COUNT = 3;

    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);

    private static final Rect2i TEXT_BBOX = new Rect2i(0, 60, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i REQUEST_BBOX = new Rect2i(0, 38, GUI_WIDTH, ROW_HEIGHT);

    private final Identifier texture;

    protected final ArrayList<Object> lines = new ArrayList<>();
    private final Scrollbar scrollbar;
    private final List<RequestWidget> requestWidgets = new ArrayList<>();

    protected boolean refreshList;
    protected int rowAmount;

    @SuppressWarnings("AssignmentToSuperclassField")
    protected AbstractRequesterScreen(
        M menu, Inventory playerInventory, Component name, ScreenStyle style, Identifier texture
    ) {
        super(menu, playerInventory, name, style);
        this.texture = texture;
        scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        imageWidth = GUI_WIDTH;
    }

    @Override
    public void addSubWidget(String id, AbstractWidget widget, Map<String, AbstractWidget> subWidgets) {
        if (widget.isFocused()) widget.setFocused(false);
        widget.setX(widget.getX() + leftPos);
        widget.setY(widget.getY() + topPos);
        subWidgets.put(id, widget);
        Utils.cast(widgets, WidgetContainerMixin.class).merequester$getWidgets().put(id, widget);
        addRenderableWidget(widget);
    }

    @Nullable
    @Override
    public Request getTargetRequest(int listIndex) {
        if (listIndex >= lines.size()) return null;
        var lineElement = lines.get(scrollbar.getCurrentScroll() + listIndex);
        return lineElement instanceof Request request ? request : null;
    }

    @Nullable
    @Override
    public List<Component> getEmptyingTooltip(RequestSlot slot, ItemStack carried) {
        var emptyingAction = getEmptyingAction(slot, carried);
        if (emptyingAction == null) return null;
        return Tooltips.getEmptyingTooltip(ButtonToolTips.SetAction, carried, emptyingAction);
    }

    public void updateFromMenu(boolean clearData, long requesterId, RequesterSyncPacket.Payload data) {
        if (clearData) {
            clear();
            refreshList();
            return;
        }

        var requests = getById(requesterId, data.name(), data.sortBy()).getRequestManager();
        for (var update : data.updates()) {
            var index = update.index();
            if (index < 0 || index >= requests.size()) continue;
            requests.get(index).fromComponent(update.component());
        }

        if (refreshList) refreshList();
    }

    @Override
    @MustBeInvokedByOverriders
    protected void init() {
        imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + rowAmount * ROW_HEIGHT + 1;

        requestWidgets.forEach(w -> w.preInit(Utils.cast(widgets, WidgetContainerMixin.class).merequester$getWidgets()));
        super.init();
        // clear old widgets because init() is recalled when the terminal resizes
        requestWidgets.clear();
        for (var i = 0; i < rowAmount; i++) {
            var requestWidget = new RequestWidget(this, i, GUI_PADDING_X, (i + 1) * ROW_HEIGHT + 1, style);
            requestWidget.postInit();
            requestWidgets.add(requestWidget);
        }

        resetScrollbar();
    }

    @Nullable
    @Override
    protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        if (slot instanceof RequestSlot requestSlot) {
            var emptyingAction = ContainerItemStrategies.getEmptyingAction(carried);
            if (emptyingAction == null) return null;

            var wrappedStack = GenericStack.wrapInItemStack(new GenericStack(emptyingAction.what(), 1));
            if (!requestSlot.getInventory().isItemValid(requestSlot.getSlot(), wrappedStack)) return null;

            return emptyingAction;
        }
        return super.getEmptyingAction(slot, carried);
    }

    @Override
    public void drawFG(GuiGraphicsExtractor guiGraphics, int pX, int pY, int mX, int mY) {
        menu.slots.removeIf(RequestSlot.class::isInstance);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();

        if (lines.isEmpty()) {
            var text = Utils.translate("gui", "no_requesters").getString();
            var textWidth = font.width(text);
            guiGraphics.text(
                font,
                text,
                (int) ((GUI_WIDTH - textWidth) / 2f - 10),
                GUI_PADDING_Y + GUI_HEADER_HEIGHT,
                textColor,
                false
            );
            requestWidgets.forEach(RequestWidget::hide);
            return;
        }

        int scrollLevel = scrollbar.getCurrentScroll();

        for (var i = 0; i < rowAmount; i++) {
            if (scrollLevel + i >= lines.size()) {
                requestWidgets.get(i).hide();
                continue;
            }

            var lineElement = lines.get(scrollLevel + i);
            if (lineElement instanceof Request request) {
                menu.slots.add(createSlot(i, request));
                requestWidgets.get(i).applyRequest(request);
            } else if (lineElement instanceof String name) {
                var text = name;
                int rows = getByName(name).size();
                if (rows > 1) text = String.format("%s (%s)", text, rows);
                text = font.plainSubstrByWidth(text, TEXT_MAX_WIDTH, true);

                guiGraphics.text(
                    font,
                    text,
                    GUI_PADDING_X + TEXT_MARGIN_X,
                    GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                    textColor,
                    false
                );
                requestWidgets.get(i).hide();
            } else {
                MERequester.LOGGER.debug("Unknown line element: {}", lineElement);
            }
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int slotIndex, int mouseButton, ContainerInput containerInput) {
        if (!(slot instanceof RequestSlot requestSlot)) {
            super.slotClicked(slot, slotIndex, mouseButton, containerInput);
            return;
        }

        if (requestSlot.isLocked()) return;

        // fluid container handler
        if (mouseButton == InputConstants.MOUSE_BUTTON_RIGHT && getEmptyingAction(slot, menu.getCarried()) != null) {
            var packet = new InventoryActionPacket(
                InventoryAction.EMPTY_ITEM,
                requestSlot.getSlot(),
                requestSlot.getRequesterReference().getRequesterId()
            );
            ClientPacketDistributor.sendToServer(packet);
            return;
        }

        InventoryAction action = null;
        switch (containerInput) {
            case PICKUP -> action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
            case QUICK_MOVE -> action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
            case CLONE -> {
                if (getPlayer().getAbilities().instabuild) {
                    action = InventoryAction.CREATIVE_DUPLICATE;
                }
            }
            default -> {
            }
        }

        if (action != null) {
            InventoryActionPacket packet = new InventoryActionPacket(
                action,
                requestSlot.getSlot(),
                requestSlot.getRequesterReference().getRequesterId()
            );
            ClientPacketDistributor.sendToServer(packet);
        }
    }

    @Override
    public void drawBG(GuiGraphicsExtractor guiGraphics, int pX, int pY, int mX, int mY, float partial) {
        blit(guiGraphics, pX, pY, HEADER_BBOX);

        int scrollLevel = scrollbar.getCurrentScroll();
        int currentY = pY + GUI_HEADER_HEIGHT;

        blit(guiGraphics, pX, currentY + rowAmount * ROW_HEIGHT, getFooterBounds());

        for (var i = 0; i < rowAmount; i++) {
            var isRequestElement = false;
            if (scrollLevel + i < lines.size()) {
                Object lineElement = lines.get(scrollLevel + i);
                isRequestElement = lineElement instanceof Request;
            }

            blit(guiGraphics, pX, currentY, isRequestElement ? REQUEST_BBOX : TEXT_BBOX);

            currentY += ROW_HEIGHT;
        }
    }

    protected void resetScrollbar() {
        scrollbar.setHeight(rowAmount * ROW_HEIGHT + 1);
        scrollbar.setRange(0, lines.size() - rowAmount, 2);
    }

    protected abstract void clear();

    protected abstract void refreshList();

    protected abstract Set<RequesterReference> getByName(String name);

    protected abstract RequesterReference getById(long requesterId, String name, long sortBy);

    private void blit(GuiGraphicsExtractor guiGraphics, int pX, int pY, Rect2i srcRect) {
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            texture,
            pX,
            pY,
            srcRect.getX(),
            srcRect.getY(),
            srcRect.getWidth(),
            srcRect.getHeight(),
            256,
            256
        );
    }

    private RequestSlot createSlot(int index, Request request) {
        var slot = new RequestSlot(
            this,
            (RequesterReference) request.getRequesterReference(),
            request.getIndex(),
            ROW_HEIGHT + GUI_PADDING_X,
            (index + 1) * ROW_HEIGHT + 2
        );
        slot.setHideAmount(true);
        slot.setLocked(request.getClientStatus().locksRequest());
        return slot;
    }

    protected abstract Rect2i getFooterBounds();
}
