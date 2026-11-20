package com.almostreliable.merequester.client;

import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import com.almostreliable.merequester.client.abstraction.RequesterReference;
import com.almostreliable.merequester.requester.Request;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.stacks.AEKey;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import com.google.common.collect.HashMultimap;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class RequesterTerminalScreen<T extends RequesterTerminalMenu> extends AbstractRequesterScreen<T> {

    private final HashMap<Long, RequesterReference> byId = new HashMap<>();
    private final HashMultimap<String, RequesterReference> byName = HashMultimap.create();

    private final List<String> requesterNames = new ArrayList<>();
    private final Map<String, Set<Object>> searchCache = new WeakHashMap<>();
    private final AETextField searchField;

    public RequesterTerminalScreen(T menu, Inventory playerInventory, Component name, ScreenStyle style) {
        super(menu, playerInventory, name, style);

        addToLeftToolbar(new SettingToggleButton<>(
            Settings.TERMINAL_STYLE,
            AEConfig.instance().getTerminalStyle(),
            this::toggleTerminalStyle
        ));

        searchField = widgets.addTextField("search");
        searchField.setResponder(_ -> refreshList());
        searchField.setPlaceholder(GuiText.SearchPlaceholder.text());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && searchField.isMouseOver(event.x(), event.y())) {
            searchField.setValue("");
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return event.codepointAsString().equals(" ") && searchField.getValue().isEmpty() || super.charTyped(event);
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

        var searchQuery = searchField.getValue().toLowerCase(Locale.ROOT);
        var cachedSearch = searchByQuery(searchQuery);
        var rebuild = cachedSearch.isEmpty();

        for (var requester : byId.values()) {
            if (!rebuild && !cachedSearch.contains(requester)) continue;

            boolean found = searchQuery.isEmpty();
            if (!found) {
                var requests = requester.getRequestManager();
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

        for (var name : requesterNames) {
            lines.add(name);
            List<RequesterReference> requesters = new ArrayList<>(byName.get(name));
            Collections.sort(requesters);
            List<Request> requests = new ArrayList<>();
            for (var requester : requesters) {
                for (var i = 0; i < requester.getRequestManager().size(); i++) {
                    requests.add(requester.getRequestManager().get(i));
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
    protected RequesterReference getById(long requesterId, String name, long sortBy, int requestCount) {
        RequesterReference requester = byId.get(requesterId);
        if (requester == null) {
            requester = new RequesterReference(requesterId, name, sortBy, requestCount);
            byId.put(requesterId, requester);
            refreshList = true;
        }
        return requester;
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

    private boolean keyMatchesSearchQuery(@Nullable AEKey key, String searchTerm) {
        return key != null && key.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(searchTerm);
    }

    private Set<Object> searchByQuery(String searchQuery) {
        Set<Object> cache = searchCache.computeIfAbsent(searchQuery, _ -> new HashSet<>());

        if (cache.isEmpty() && searchQuery.length() > 1) {
            cache.addAll(searchByQuery(searchQuery.substring(0, searchQuery.length() - 1)));
        }
        return cache;
    }
}
