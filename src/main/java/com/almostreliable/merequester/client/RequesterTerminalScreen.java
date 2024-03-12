package com.almostreliable.merequester.client;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.stacks.AEKey;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.requester.Requests.Request;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.google.common.collect.HashMultimap;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nullable;
import java.util.*;

import static com.almostreliable.merequester.Utils.f;

public class RequesterTerminalScreen<T extends RequesterTerminalMenu> extends AbstractRequesterScreen<T> {

    private static final ResourceLocation TEXTURE = Utils.getRL(f("textures/gui/{}.png", MERequester.TERMINAL_ID));
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 133, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private final HashMap<Long, RequesterReference> byId = new HashMap<>();
    private final HashMultimap<String, RequesterReference> byName = HashMultimap.create();

    private final List<String> requesterNames = new ArrayList<>();
    private final Map<String, Set<Object>> searchCache = new WeakHashMap<>();
    private final AETextField searchField;

    public RequesterTerminalScreen(
        T menu, Inventory playerInventory, Component name, ScreenStyle style
    ) {
        super(menu, playerInventory, name, style, TEXTURE);

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
    public boolean mouseClicked(double mX, double mY, int button) {
        if (button == 1 && searchField.isMouseOver(mX, mY)) {
            searchField.setValue("");
        }
        return super.mouseClicked(mX, mY, button);
    }

    @Override
    public boolean charTyped(char character, int key) {
        return character == ' ' && searchField.getValue().isEmpty() || super.charTyped(character, key);
    }

    @Override
    protected void init() {
        var availableHeight = height - 2 * config.getTerminalMargin();
        var possibleRows = (availableHeight - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        rowAmount = Math.max(MIN_ROW_COUNT, config.getTerminalStyle().getRows(possibleRows));

        super.init();

        setInitialFocus(searchField);
    }

    @Override
    protected void clear() {
        byId.clear();
    }

    @Override
    protected void refreshList() {
        refreshList = false;
        searchCache.clear();
        byName.clear();

        var searchQuery = searchField.getValue().toLowerCase();
        var cachedSearch = searchByQuery(searchQuery);
        var rebuild = cachedSearch.isEmpty();

        for (var requester : byId.values()) {
            if (!rebuild && !cachedSearch.contains(requester)) continue;

            boolean found = searchQuery.isEmpty();
            if (!found) {
                var requests = requester.getRequests();
                for (var i = 0; i < requests.size(); i++) {
                    found = keyMatchesSearchQuery(requests.getKey(i), searchQuery);
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

        lines.clear();
        lines.ensureCapacity(requesterNames.size() + byId.size() * Platform.getRequestLimit());

        for (var name : requesterNames) {
            lines.add(name);
            List<RequesterReference> requesters = new ArrayList<>(byName.get(name));
            Collections.sort(requesters);
            List<Request> requests = new ArrayList<>();
            for (var requester : requesters) {
                for (var i = 0; i < requester.getRequests().size(); i++) {
                    requests.add(requester.getRequests().get(i));
                }
            }
            lines.addAll(requests);
        }

        resetScrollbar();
    }

    @Override
    protected Set<RequesterReference> getByName(String name) {
        return byName.get(name);
    }

    @Override
    protected RequesterReference getById(long requesterId, String name, long sortBy) {
        RequesterReference requester = byId.get(requesterId);
        if (requester == null) {
            requester = new RequesterReference(requesterId, name, sortBy);
            byId.put(requesterId, requester);
            refreshList = true;
        }
        return requester;
    }

    @Override
    protected Rect2i getFooterBounds() {
        return FOOTER_BBOX;
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> button, boolean backwards) {
        TerminalStyle next = button.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        button.set(next);
        reinitialize();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void reinitialize() {
        var renderableWidgets = Platform.getRenderables(this);
        children().removeAll(renderableWidgets);
        renderableWidgets.clear();
        init();
    }

    private boolean keyMatchesSearchQuery(@Nullable AEKey key, String searchTerm) {
        return key != null && key.getDisplayName().getString().toLowerCase().contains(searchTerm);
    }

    private Set<Object> searchByQuery(String searchQuery) {
        Set<Object> cache = searchCache.computeIfAbsent(searchQuery, $ -> new HashSet<>());

        if (cache.isEmpty() && searchQuery.length() > 1) {
            cache.addAll(searchByQuery(searchQuery.substring(0, searchQuery.length() - 1)));
        }
        return cache;
    }
}
