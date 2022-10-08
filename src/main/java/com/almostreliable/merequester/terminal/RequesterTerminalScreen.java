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
import appeng.menu.slot.FakeSlot;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.requester.RequesterRecord;
import com.almostreliable.merequester.Utils;
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

    private static final int GUI_HEADER_HEIGHT = 17;
    private static final int GUI_FOOTER_HEIGHT = 97;

    private static final int NAME_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 155;

    private static final int ROW_HEIGHT = 18;
    private static final int DEFAULT_ROW_COUNT = 5;
    private static final int MIN_ROW_COUNT = 3;

    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 125, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private static final Rect2i ROW_TEXT_TOP_BBOX = new Rect2i(0, 17, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_MIDDLE_BBOX = new Rect2i(0, 53, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_BOTTOM_BBOX = new Rect2i(0, 89, GUI_WIDTH, ROW_HEIGHT);

    private static final Rect2i ROW_INVENTORY_TOP_BBOX = new Rect2i(0, 35, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_MIDDLE_BBOX = new Rect2i(0, 71, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_BOTTOM_BBOX = new Rect2i(0, 107, GUI_WIDTH, ROW_HEIGHT);

    private final HashMap<Long, RequesterRecord> byId = new HashMap<>();
    private final HashMultimap<String, RequesterRecord> byName = HashMultimap.create();
    private final List<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();
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
        // TODO: check if this is done every render tick and maybe move it to init
        menu.slots.removeIf(FakeSlot.class::isInstance);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        int scrollLevel = scrollbar.getCurrentScroll();

        for (var i = 0; i < rowAmount; ++i) {
            if (scrollLevel + i < lines.size()) {
                var lineObj = lines.get(scrollLevel + i);
                if (lineObj instanceof RequesterRecord host) {
                    for (int z = 0; z < host.getRequests().size(); z++) {
                        menu.slots.add(new RequestSlot(host, z, z * ROW_HEIGHT + GUI_PADDING_X, (i + 1) * ROW_HEIGHT));
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
                        GUI_PADDING_X + NAME_MARGIN_X,
                        GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                        textColor
                    );
                }
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
            boolean firstLine = i == 0;
            boolean lastLine = i == rowAmount - 1;

            var isInvLine = false;
            if (scrollLevel + i < lines.size()) {
                Object lineObj = lines.get(scrollLevel + i);
                isInvLine = lineObj instanceof RequesterRecord;
            }

            blit(poseStack, pX, currentY, selectBox(isInvLine, firstLine, lastLine));

            currentY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        return character == ' ' && searchField.getValue().isEmpty() || super.charTyped(character, key);
    }

    private Rect2i selectBox(boolean isInvLine, boolean firstLine, boolean lastLine) {
        if (isInvLine) {
            if (firstLine) return ROW_INVENTORY_TOP_BBOX;
            if (lastLine) return ROW_INVENTORY_BOTTOM_BBOX;
            return ROW_INVENTORY_MIDDLE_BBOX;
        }

        if (firstLine) return ROW_TEXT_TOP_BBOX;
        if (lastLine) return ROW_TEXT_BOTTOM_BBOX;
        return ROW_TEXT_MIDDLE_BBOX;
    }

    public void postInventoryUpdate(boolean clearExistingData, long inventoryId, CompoundTag invData) {
        if (clearExistingData) {
            byId.clear();
            refreshList = true;
        } else {
            var un = Component.Serializer.fromJson(invData.getString("un"));
            if (un == null) return;
            var requester = getById(inventoryId, invData.getLong("sortBy"), un);

            for (int i = 0; i < requester.getRequests().size(); i++) {
                String which = Integer.toString(i);
                if (invData.contains(which)) {
                    requester.getRequests().setItemDirect(i, ItemStack.of(invData.getCompound(which)));
                }
            }
        }

        if (refreshList) {
            refreshList = false;
            cachedSearches.clear();
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
        scrollbar.setHeight(rowAmount * ROW_HEIGHT - 2);
        scrollbar.setRange(0, lines.size() - rowAmount, 2);
    }

    private void refreshList() {
        byName.clear();

        String searchFilterLowerCase = searchField.getValue().toLowerCase();
        Set<Object> cachedSearch = getCacheForSearchTerm(searchFilterLowerCase);
        boolean rebuild = cachedSearch.isEmpty();

        for (RequesterRecord entry : byId.values()) {
            if (!rebuild && !cachedSearch.contains(entry)) continue;

            boolean found = searchFilterLowerCase.isEmpty();
            if (!found) {
                for (ItemStack itemStack : entry.getRequests()) {
                    found = itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
                    if (found) break;
                }
            }

            if (found || entry.getSearchName().contains(searchFilterLowerCase)) {
                byName.put(entry.getDisplayName(), entry);
                cachedSearch.add(entry);
            } else {
                cachedSearch.remove(entry);
            }
        }

        names.clear();
        names.addAll(byName.keySet());

        Collections.sort(names);

        lines.clear();
        lines.ensureCapacity(names.size() + byId.size());

        for (String n : names) {
            lines.add(n);
            List<RequesterRecord> clientInventories = new ArrayList<>(byName.get(n));
            Collections.sort(clientInventories);
            lines.addAll(clientInventories);
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

    private boolean itemStackMatchesSearchTerm(ItemStack itemStack, String searchTerm) {
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

    private Set<Object> getCacheForSearchTerm(String searchTerm) {
        Set<Object> cache = cachedSearches.computeIfAbsent(searchTerm, $ -> new HashSet<>());

        if (cache.isEmpty() && searchTerm.length() > 1) {
            cache.addAll(getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
        }
        return cache;
    }
}
