package com.almostreliable.merequester.compat.wtlib;

import com.almostreliable.merequester.Utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.features.GridLinkables;
import appeng.client.InitScreens;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"NonConstantFieldWithUpperCaseName", "StaticVariableMayNotBeInitialized"})
public final class WirelessTerminalCompat {

    public static final WirelessTerminalCompat INSTANCE = new WirelessTerminalCompat();
    static final String TERMINAL_ID = "wireless_requester_terminal";

    public void init(DeferredRegister<MenuType<?>> menuRegistry) {
        if (isLoaded()) {
            Guard.init(menuRegistry);
        }
    }

    public void initClient(RegisterMenuScreensEvent event) {
        if (isLoaded()) {
            GuardClient.init(event);
        }
    }

    public void registerWirelessTerminal(Registry<Item> registry) {
        if (isLoaded()) {
            Guard.registerWirelessTerminal(registry);
        }
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (isLoaded()) {
            Guard.registerCapabilities(event);
        }
    }

    public Iterable<ItemLike> collectItems() {
        if (!isLoaded()) {
            return List.of();
        }

        return Guard.collectItems();
    }

    private boolean isLoaded() {
        return ModList.get().getModFileById("ae2wtlib") != null;
    }

    private WirelessTerminalCompat() {}

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    private static final class Guard {

        private static final Icon.Texture ICON_TEXTURE = new Icon.Texture(Utils.getRL("textures/wtlib/icon.png"), 16, 16);
        private static final Icon ICON = new Icon(0, 0, 16, 16, ICON_TEXTURE);

        @Nullable
        private static ReqWirelessTerminalItem WIRELESS_REQUESTER_TERMINAL;

        private static void init(DeferredRegister<MenuType<?>> menuRegistry) {
            menuRegistry.register(TERMINAL_ID, () -> ReqWirelessTerminalMenu.TYPE);
        }

        private static void registerWirelessTerminal(Registry<Item> registry) {
            var terminalId = Utils.getRL(TERMINAL_ID);
            var terminalKey = ResourceKey.create(Registries.ITEM, terminalId);
            var terminalItem = new ReqWirelessTerminalItem(new Item.Properties().setId(terminalKey));

            Registry.register(registry, terminalId, terminalItem);
            AddTerminalEvent.register(event -> event.builder(
                    "requester",
                    ReqWirelessTerminalMenuHost::new,
                    ReqWirelessTerminalMenu.TYPE,
                    terminalItem,
                    ICON
                )
                .addTerminal());

            WIRELESS_REQUESTER_TERMINAL = terminalItem;
        }

        private static void registerCapabilities(RegisterCapabilitiesEvent event) {
            assert WIRELESS_REQUESTER_TERMINAL != null;

            GridLinkables.register(WIRELESS_REQUESTER_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
            event.registerItem(
                Capabilities.Energy.ITEM,
                (_, context) -> new PoweredItemCapabilities(context, WIRELESS_REQUESTER_TERMINAL, WIRELESS_REQUESTER_TERMINAL),
                WIRELESS_REQUESTER_TERMINAL
            );
        }

        private static Iterable<ItemLike> collectItems() {
            assert WIRELESS_REQUESTER_TERMINAL != null;
            return List.of(WIRELESS_REQUESTER_TERMINAL);
        }
    }

    private static final class GuardClient {

        private static void init(RegisterMenuScreensEvent event) {
            InitScreens.register(
                event,
                ReqWirelessTerminalMenu.TYPE,
                ReqWirelessTerminalScreen::new,
                String.format("/screens/%s.json", TERMINAL_ID)
            );
        }
    }
}
