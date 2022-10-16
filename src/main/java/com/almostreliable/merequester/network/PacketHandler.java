package com.almostreliable.merequester.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class PacketHandler {

    private PacketHandler() {}

    public static void initC2S() {
        ServerPlayNetworking.registerGlobalReceiver(RequestUpdatePacket.CHANNEL, RequestUpdatePacket::handlePacket);
        ServerPlayNetworking.registerGlobalReceiver(DragAndDropPacket.CHANNEL, DragAndDropPacket::handlePacket);
    }

    public static void initS2C() {
        ClientPlayNetworking.registerGlobalReceiver(RequesterSyncPacket.CHANNEL, RequesterSyncPacket::handlePacket);
    }
}
