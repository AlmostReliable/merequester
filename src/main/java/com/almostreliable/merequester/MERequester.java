package com.almostreliable.merequester;

import appeng.api.IAEAddonEntrypoint;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.platform.Platform;
import com.almostreliable.merequester.wireless.WirelessRequesterTerminalRegistration;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
public final class MERequester implements IAEAddonEntrypoint {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    @Override
    public void onAe2Initialized() {
        Platform.initConfig();
        PacketHandler.initC2S();
        if(!Platform.isModLoaded("ae2wtlib")) WirelessRequesterTerminalRegistration.registerWirelessRequesterTerminal();
    }
}
