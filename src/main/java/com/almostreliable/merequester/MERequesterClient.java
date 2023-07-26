package com.almostreliable.merequester;

import com.almostreliable.merequester.network.PacketHandler;
import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("WeakerAccess")
public class MERequesterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PacketHandler.initS2C();
    }
}
