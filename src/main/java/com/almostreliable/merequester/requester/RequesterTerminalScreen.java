package com.almostreliable.merequester.requester;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.TypeFilter;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class RequesterTerminalScreen extends AEBaseScreen<RequesterTerminalMenu> {

    private static final int GUI_WIDTH = 195;

    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;

    private static final int GUI_HEADER_HEIGHT = 17;
    private static final int GUI_FOOTER_HEIGHT = 97;

    private static final int ROW_HEIGHT = 18;
    private static final int DEFAULT_ROW_COUNT = 5;
    private static final int MIN_ROW_COUNT = 3;

    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 125, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private final Scrollbar scrollbar;
    private final AETextField searchField;

    private int rowAmount = 0;

    private final ServerSettingToggleButton<TypeFilter> typeFilter;

    @SuppressWarnings("AssignmentToSuperclassField")
    public RequesterTerminalScreen(
        RequesterTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style
    ) {
        super(menu, playerInventory, title, style);
        scrollbar = widgets.addScrollBar("scrollbar");
        imageWidth = GUI_WIDTH;

        addToLeftToolbar(new SettingToggleButton<>(Settings.TERMINAL_STYLE, AEConfig.instance().getTerminalStyle(), this::togleTerminalStyle));
        typeFilter = new ServerSettingToggleButton<>(Settings.TYPE_FILTER, TypeFilter.ALL);
        addToLeftToolbar(typeFilter);

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
}
