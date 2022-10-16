package com.almostreliable.merequester;

import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.platform.Platform;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
public final class MERequester implements ModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    @Override
    public void onInitialize() {
        Platform.initConfig();
        PacketHandler.initC2S();
    }
}
