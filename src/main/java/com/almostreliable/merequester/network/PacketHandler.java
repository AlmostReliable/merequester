package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketHandler {

    private static final ResourceLocation ID = Utils.getRL("network");
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(ID)
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(PROTOCOL::equals)
        .serverAcceptedVersions(PROTOCOL::equals)
        .simpleChannel();

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init() {
        var packetId = -1;
        // server to client
        register(++packetId, RequesterTerminalPacket.class, new RequesterTerminalPacket());
        // client to server
        register(++packetId, RequestStatePacket.class, new RequestStatePacket());
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> void register(int packetId, Class<T> clazz, Packet<T> packet) {
        CHANNEL.registerMessage(packetId, clazz, packet::encode, packet::decode, packet::handle);
    }
}
