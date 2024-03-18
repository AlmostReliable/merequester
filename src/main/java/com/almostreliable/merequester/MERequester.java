package com.almostreliable.merequester;

import com.almostreliable.merequester.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String REQUESTER_ID = "requester";
    public static final String TERMINAL_ID = "requester_terminal";

    public MERequester(IEventBus modEventBus) {
        modEventBus.addListener(MERequester::onRegistryEvent);
        modEventBus.addListener(MERequester::onCreativeTabContents);
        modEventBus.addListener(PacketHandler::onPacketRegistration);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private static void onRegistryEvent(RegisterEvent event) {
        Registration.registerContents(event);
    }

    private static void onCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        Registration.Tab.initContents(event);
    }
}
