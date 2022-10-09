package com.almostreliable.merequester.terminal;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.stacks.AEItemKey;
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
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.RequesterRecord;
import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

/**
 * yoinked from {@link PatternAccessTermScreen}
 */
public class RequesterTerminalScreen extends AEBaseScreen<RequesterTerminalMenu> {

    private static final ResourceLocation TEXTURE = Utils.getRL("textures/gui/" + MERequester.TERMINAL_ID + ".png");

    private static final int GUI_WIDTH = 195;
    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;
    private static final int GUI_HEADER_HEIGHT = 18;
    private static final int GUI_FOOTER_HEIGHT = 98;

    private static final int TEXT_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 156;

    private static final int ROW_HEIGHT = 19;
    private static final int DEFAULT_ROW_COUNT = RequesterBlockEntity.SLOTS + 1;
    private static final int MIN_ROW_COUNT = 3;

    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 132, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private static final Rect2i TEXT_BBOX = new Rect2i(0, 18, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i REQUEST_BBOX = new Rect2i(0, 37, GUI_WIDTH, ROW_HEIGHT);

    private final HashMap<Long, RequesterRecord> byId = new HashMap<>();
    private final HashMultimap<String, RequesterRecord> byName = HashMultimap.create();

    private final List<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();
    private final Map<String, Set<Object>> searchCache = new WeakHashMap<>();

    private final Scrollbar scrollbar;
    private final AETextField searchField;

    private boolean refreshList;
    private int rowAmount;

    @SuppressWarnings("AssignmentToSuperclassField")
    public RequesterTerminalScreen(
        RequesterTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style
    ) {
        super(menu, playerInventory, title, style);
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
    protected void init() {
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        var maxRows = terminalStyle == TerminalStyle.SMALL ? DEFAULT_ROW_COUNT : Integer.MAX_VALUE;
        rowAmount = (height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        rowAmount = Mth.clamp(rowAmount, MIN_ROW_COUNT, maxRows);

        imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + rowAmount * ROW_HEIGHT;

        super.init();

        setInitialFocus(searchField);
        resetScrollbar();
    }

    @Override
    public void drawFG(PoseStack poseStack, int pX, int pY, int mX, int mY) {
        menu.slots.removeIf(RequestSlot.class::isInstance);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        int scrollLevel = scrollbar.getCurrentScroll();

        for (var i = 0; i < rowAmount; ++i) {
            if (scrollLevel + i >= lines.size()) continue;

            var lineObj = lines.get(scrollLevel + i);
            if (lineObj instanceof RequesterRecord host) {
                for (int j = 0; j < host.getRequests().size(); j++) {
                    menu.slots.add(new RequestSlot(host, j, j * ROW_HEIGHT + GUI_PADDING_X, (i + 1) * ROW_HEIGHT));
                }
            } else if (lineObj instanceof String name) {
                int rows = byName.get(name).size();
                if (rows > 1) {
                    name = name + " (" + rows + ')';
                }

                name = font.plainSubstrByWidth(name, TEXT_MAX_WIDTH, true);

                font.draw(
                    poseStack,
                    name,
                    GUI_PADDING_X + TEXT_MARGIN_X,
                    GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                    textColor
                );
            }
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (slot instanceof RequestSlot requestSlot) {
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
                case THROW:
                default:
            }

            if (action != null) {
                InventoryActionPacket p = new InventoryActionPacket(
                    action,
                    requestSlot.getSlot(),
                    requestSlot.getHost().getServerId()
                );
                NetworkHandler.instance().sendToServer(p);
            }
            return;
        }

        super.slotClicked(slot, slotId, mouseButton, clickType);
    }

    @Override
    public boolean mouseClicked(double mX, double mY, int button) {
        if (button == 1 && searchField.isMouseOver(mX, mY)) {
            searchField.setValue("");
        }
        return super.mouseClicked(mX, mY, button);
    }

    @Override
    public void drawBG(PoseStack poseStack, int pX, int pY, int mX, int mY, float partial) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        blit(poseStack, pX, pY, HEADER_BBOX);

        int scrollLevel = scrollbar.getCurrentScroll();
        int currentY = pY + GUI_HEADER_HEIGHT;

        blit(poseStack, pX, currentY + rowAmount * ROW_HEIGHT, FOOTER_BBOX);

        for (int i = 0; i < rowAmount; ++i) {
            var isInvLine = false;
            if (scrollLevel + i < lines.size()) {
                Object lineObj = lines.get(scrollLevel + i);
                isInvLine = lineObj instanceof RequesterRecord;
            }

            blit(poseStack, pX, currentY, selectBox(isInvLine));

            currentY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        return character == ' ' && searchField.getValue().isEmpty() || super.charTyped(character, key);
    }

    private Rect2i selectBox(boolean isInvLine) {
        return isInvLine ? REQUEST_BBOX : TEXT_BBOX;
    }

    public void postInventoryUpdate(boolean clearExistingData, long inventoryId, CompoundTag invData) {
        if (clearExistingData) {
            byId.clear();
            refreshList = true;
        } else {
            var un = Component.Serializer.fromJson(invData.getString("un"));
            if (un == null) return;
            var requester = getById(inventoryId, invData.getLong("sortBy"), un);

            // TODO: debug if this is the correct logic
            for (int i = 0; i < requester.getRequests().size(); i++) {
                String which = Integer.toString(i);
                if (invData.contains(which)) {
                    requester.getRequests().get(i).deserializeNBT(invData.getCompound(which));
                }
            }
        }

        if (refreshList) {
            refreshList = false;
            searchCache.clear();
            refreshList();
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
        scrollbar.setHeight(rowAmount * ROW_HEIGHT);
        scrollbar.setRange(0, lines.size() - rowAmount, 2);
    }

    private void refreshList() {
        byName.clear();

        var searchQuery = searchField.getValue().toLowerCase();
        var cachedSearch = searchByQuery(searchQuery);
        var rebuild = cachedSearch.isEmpty();

        for (RequesterRecord requester : byId.values()) {
            if (!rebuild && !cachedSearch.contains(requester)) continue;

            boolean found = searchQuery.isEmpty();
            if (!found) {
                for (var stack : requester.getRequests()) {
                    found = stackMatchesSearchQuery(stack, searchQuery);
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

        names.clear();
        names.addAll(byName.keySet());
        Collections.sort(names);

        lines.clear();
        lines.ensureCapacity(names.size() + byId.size());

        for (var name : names) {
            lines.add(name);
            List<RequesterRecord> requesters = new ArrayList<>(byName.get(name));
            Collections.sort(requesters);
            lines.addAll(requesters);
        }

        resetScrollbar();
    }

    /**
     * A version of blit that accepts a source rectangle.
     *
     * @see GuiComponent#blit(PoseStack, int, int, int, int, int, int)
     */
    private void blit(PoseStack poseStack, int pX, int pY, Rect2i srcRect) {
        blit(poseStack, pX, pY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

    private boolean stackMatchesSearchQuery(ItemStack itemStack, String searchTerm) {
        if (itemStack.isEmpty()) return false;

        CompoundTag encodedValue = itemStack.getTag();
        if (encodedValue == null) return false;

        ListTag outTag = encodedValue.getList("out", Tag.TAG_COMPOUND);
        for (int i = 0; i < outTag.size(); i++) {
            var parsedItemStack = ItemStack.of(outTag.getCompound(i));
            var itemKey = AEItemKey.of(parsedItemStack);
            if (itemKey != null) {
                var displayName = itemKey.getDisplayName().getString().toLowerCase();
                if (displayName.contains(searchTerm)) {
                    return true;
                }
            }
        }
        return false;
    }

    private RequesterRecord getById(long id, long sortBy, Component name) {
        RequesterRecord o = byId.get(id);
        if (o == null) {
            o = new RequesterRecord(id, sortBy, name);
            byId.put(id, o);
            refreshList = true;
        }
        return o;
    }

    private Set<Object> searchByQuery(String searchQuery) {
        Set<Object> cache = searchCache.computeIfAbsent(searchQuery, $ -> new HashSet<>());

        if (cache.isEmpty() && searchQuery.length() > 1) {
            cache.addAll(searchByQuery(searchQuery.substring(0, searchQuery.length() - 1)));
        }
        return cache;
    }
}
