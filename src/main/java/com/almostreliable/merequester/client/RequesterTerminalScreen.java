package com.almostreliable.merequester.client;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.widgets.RequestWidget;
import com.almostreliable.merequester.mixin.WidgetContainerMixin;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.Requests.Request;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static com.almostreliable.merequester.Utils.f;

/**
 * yoinked from {@link PatternAccessTermScreen}
 */
public class RequesterTerminalScreen extends AEBaseScreen<RequesterTerminalMenu> implements RequestDisplay {

    private static final ResourceLocation TEXTURE = Utils.getRL(f("textures/gui/{}.png", MERequester.TERMINAL_ID));

    private static final int GUI_WIDTH = 195;
    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;
    private static final int GUI_HEADER_HEIGHT = 19;
    private static final int GUI_FOOTER_HEIGHT = 98;

    private static final int TEXT_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 156;

    private static final int ROW_HEIGHT = 19;
    private static final int DEFAULT_ROW_COUNT = RequesterBlockEntity.SLOTS + 1;
    private static final int MIN_ROW_COUNT = 3;

    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 133, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private static final Rect2i TEXT_BBOX = new Rect2i(0, 19, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i REQUEST_BBOX = new Rect2i(0, 38, GUI_WIDTH, ROW_HEIGHT);

    private final HashMap<Long, RequesterReference> byId = new HashMap<>();
    private final HashMultimap<String, RequesterReference> byName = HashMultimap.create();

    private final List<String> requesterNames = new ArrayList<>();
    private final ArrayList<Object> linesToRender = new ArrayList<>();
    private final Map<String, Set<Object>> searchCache = new WeakHashMap<>();

    private final Scrollbar scrollbar;
    private final AETextField searchField;

    private final List<RequestWidget> requestWidgets = new ArrayList<>();

    private boolean refreshList;
    private int rowAmount;

    @SuppressWarnings("AssignmentToSuperclassField")
    public RequesterTerminalScreen(
        RequesterTerminalMenu menu, Inventory playerInventory, Component name, ScreenStyle style
    ) {
        super(menu, playerInventory, name, style);
        scrollbar = widgets.addScrollBar("scrollbar");
        imageWidth = GUI_WIDTH;

        addToLeftToolbar(new SettingToggleButton<>(
            Settings.TERMINAL_STYLE,
            AEConfig.instance().getTerminalStyle(),
            this::toggleTerminalStyle
        ));

        searchField = widgets.addTextField("search");
        searchField.setResponder(str -> refreshList());
        searchField.setPlaceholder(GuiText.SearchPlaceholder.text());
    }

    @Override
    public boolean charTyped(char character, int key) {
        return character == ' ' && searchField.getValue().isEmpty() || super.charTyped(character, key);
    }

    public void updateFromMenu(boolean clearData, long requesterId, CompoundTag data) {
        if (clearData) {
            byId.clear();
            refreshList();
            return;
        }

        var name = data.getString(RequesterTerminalMenu.UNIQUE_NAME_ID);
        var sortBy = data.getLong(RequesterTerminalMenu.SORT_BY_ID);
        var requests = getById(requesterId, name, sortBy).getRequests();

        for (int i = 0; i < requests.size(); i++) {
            var requestIndex = String.valueOf(i);
            if (data.contains(requestIndex)) {
                requests.get(i).deserializeNBT(data.getCompound(requestIndex));
            }
        }

        if (refreshList) refreshList();
    }

    @Override
    protected void init() {
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        var maxRows = terminalStyle == TerminalStyle.SMALL ? DEFAULT_ROW_COUNT : Integer.MAX_VALUE;
        rowAmount = (height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        rowAmount = Mth.clamp(rowAmount, MIN_ROW_COUNT, maxRows);

        imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + rowAmount * ROW_HEIGHT;

        requestWidgets.forEach(
            w -> w.preInit(Utils.cast(widgets, WidgetContainerMixin.class).merequester$getWidgets())
        );
        super.init();
        // clear old widgets because init() is recalled when the terminal resizes
        requestWidgets.clear();
        for (var i = 0; i < rowAmount; i++) {
            var requestWidget = new RequestWidget(this, i, GUI_PADDING_X, (i + 1) * ROW_HEIGHT, style);
            requestWidget.postInit();
            requestWidgets.add(requestWidget);
        }

        setInitialFocus(searchField);
        resetScrollbar();
    }

    @Override
    public void addSubWidget(String id, AbstractWidget widget, Map<String, AbstractWidget> subWidgets) {
        if (widget.isFocused()) widget.changeFocus(false);
        widget.x += leftPos;
        widget.y += topPos;
        subWidgets.put(id, widget);
        Utils.cast(widgets, WidgetContainerMixin.class).merequester$getWidgets().put(id, widget);
        addRenderableWidget(widget);
    }

    @Override
    public void drawFG(PoseStack poseStack, int pX, int pY, int mX, int mY) {
        menu.slots.removeIf(RequestSlot.class::isInstance);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();

        if (linesToRender.isEmpty()) {
            var text = Utils.translateAsString("gui", "no_requesters");
            var textWidth = font.width(text);
            font.draw(poseStack, text, (GUI_WIDTH - textWidth) / 2f - 10, GUI_PADDING_Y + GUI_HEADER_HEIGHT, textColor);
            requestWidgets.forEach(RequestWidget::hide);
            return;
        }

        forRelevantLines(
            (i, request) -> {
                menu.slots.add(new RequestSlot(
                    (RequesterReference) request.getRequesterReference(),
                    request.getSlot(),
                    ROW_HEIGHT + GUI_PADDING_X,
                    (i + 1) * ROW_HEIGHT + 1
                ));
                requestWidgets.get(i).applyRequest(request);
            },
            (i, name) -> {
                var text = name;
                int rows = byName.get(name).size();
                if (rows > 1) text = f("{} ({})", text, rows);
                text = font.plainSubstrByWidth(text, TEXT_MAX_WIDTH, true);

                font.draw(
                    poseStack,
                    text,
                    GUI_PADDING_X + TEXT_MARGIN_X,
                    GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                    textColor
                );
                requestWidgets.get(i).hide();
            }
        );
    }

    private void forRelevantLines(BiConsumer<Integer, Request> onRequestLine, BiConsumer<Integer, String> onTextLine) {
        int scrollLevel = scrollbar.getCurrentScroll();

        for (var i = 0; i < rowAmount; i++) {
            if (scrollLevel + i >= linesToRender.size()) {
                requestWidgets.get(i).hide();
                continue;
            }

            var lineElement = linesToRender.get(scrollLevel + i);
            if (lineElement instanceof Request request) {
                onRequestLine.accept(i, request);
            } else if (lineElement instanceof String name) {
                onTextLine.accept(i, name);
            } else {
                MERequester.LOGGER.debug("Unknown line element: {}", lineElement);
            }
        }
    }

    @Nullable
    @Override
    public Request getTargetRequest(int listIndex) {
        var lineElement = linesToRender.get(scrollbar.getCurrentScroll() + listIndex);
        return lineElement instanceof Request request ? request : null;
    }

    @Override
    public boolean mouseClicked(double mX, double mY, int button) {
        if (button == 1 && searchField.isMouseOver(mX, mY)) {
            searchField.setValue("");
        }
        return super.mouseClicked(mX, mY, button);
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int slotIndex, int mouseButton, ClickType clickType) {
        if (!(slot instanceof RequestSlot requestSlot)) {
            super.slotClicked(slot, slotIndex, mouseButton, clickType);
            return;
        }

        InventoryAction action = null;
        switch (clickType) {
            case PICKUP:
                action = mouseButton == 1 ?
                    InventoryAction.SPLIT_OR_PLACE_SINGLE :
                    InventoryAction.PICKUP_OR_SET_DOWN;
                break;
            case QUICK_MOVE:
                action = mouseButton == 1 ?
                    InventoryAction.PICKUP_SINGLE :
                    InventoryAction.SHIFT_CLICK;
                break;
            case CLONE:
                if (getPlayer().getAbilities().instabuild) {
                    action = InventoryAction.CREATIVE_DUPLICATE;
                }
                break;
            default:
        }

        if (action != null) {
            InventoryActionPacket packet = new InventoryActionPacket(
                action,
                requestSlot.getSlot(),
                requestSlot.getHost().getRequesterId()
            );
            NetworkHandler.instance().sendToServer(packet);
        }
    }

    @Override
    public void drawBG(PoseStack poseStack, int pX, int pY, int mX, int mY, float partial) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        blit(poseStack, pX, pY, HEADER_BBOX);

        int scrollLevel = scrollbar.getCurrentScroll();
        int currentY = pY + GUI_HEADER_HEIGHT;

        blit(poseStack, pX, currentY + rowAmount * ROW_HEIGHT, FOOTER_BBOX);

        for (int i = 0; i < rowAmount; i++) {
            var isRequestElement = false;
            if (scrollLevel + i < linesToRender.size()) {
                Object lineElement = linesToRender.get(scrollLevel + i);
                isRequestElement = lineElement instanceof Request;
            }

            blit(poseStack, pX, currentY, isRequestElement ? REQUEST_BBOX : TEXT_BBOX);

            currentY += ROW_HEIGHT;
        }
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> button, boolean backwards) {
        TerminalStyle next = button.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        button.set(next);
        reinitialize();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void reinitialize() {
        children().removeAll(renderables);
        renderables.clear();
        init();
    }

    private void resetScrollbar() {
        scrollbar.setHeight(rowAmount * ROW_HEIGHT + 1);
        scrollbar.setRange(0, linesToRender.size() - rowAmount, 2);
    }

    private void refreshList() {
        refreshList = false;
        searchCache.clear();
        byName.clear();

        var searchQuery = searchField.getValue().toLowerCase();
        var cachedSearch = searchByQuery(searchQuery);
        var rebuild = cachedSearch.isEmpty();

        for (RequesterReference requester : byId.values()) {
            if (!rebuild && !cachedSearch.contains(requester)) continue;

            boolean found = searchQuery.isEmpty();
            if (!found) {
                for (var requestStack : requester.getRequests()) {
                    found = stackMatchesSearchQuery(requestStack, searchQuery);
                    if (found) break;
                }
            }

            if (found || requester.getSearchName().contains(searchQuery)) {
                byName.put(requester.getDisplayName(), requester);
                cachedSearch.add(requester);
            } else {
                cachedSearch.remove(requester);
            }
        }

        requesterNames.clear();
        requesterNames.addAll(byName.keySet());
        Collections.sort(requesterNames);

        linesToRender.clear();
        linesToRender.ensureCapacity(requesterNames.size() + byId.size() * RequesterBlockEntity.SLOTS);

        for (var name : requesterNames) {
            linesToRender.add(name);
            List<RequesterReference> requesters = new ArrayList<>(byName.get(name));
            Collections.sort(requesters);
            List<Request> requests = new ArrayList<>();
            for (var requester : requesters) {
                for (var i = 0; i < requester.getRequests().size(); i++) {
                    requests.add(requester.getRequests().get(i));
                }
            }
            linesToRender.addAll(requests);
        }

        resetScrollbar();
    }

    private void blit(PoseStack poseStack, int pX, int pY, Rect2i srcRect) {
        blit(poseStack, pX, pY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

    private boolean stackMatchesSearchQuery(ItemStack requestStack, String searchTerm) {
        return !requestStack.isEmpty() && requestStack.getDisplayName().getString().toLowerCase().contains(searchTerm);
    }

    private RequesterReference getById(long requesterId, String name, long sortBy) {
        RequesterReference requester = byId.get(requesterId);
        if (requester == null) {
            requester = new RequesterReference(requesterId, name, sortBy);
            byId.put(requesterId, requester);
            refreshList = true;
        }
        return requester;
    }

    private Set<Object> searchByQuery(String searchQuery) {
        Set<Object> cache = searchCache.computeIfAbsent(searchQuery, $ -> new HashSet<>());

        if (cache.isEmpty() && searchQuery.length() > 1) {
            cache.addAll(searchByQuery(searchQuery.substring(0, searchQuery.length() - 1)));
        }
        return cache;
    }
}
