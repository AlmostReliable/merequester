package com.almostreliable.merequester.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public final class PacketHandler {

    private static final String PROTOCOL = "2";

    private PacketHandler() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(PacketHandler::onPacketRegistration);
    }

    private static void onPacketRegistration(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL);

        // server to client
        registrar.playToClient(
            RequesterSyncPacket.TYPE,
            RequesterSyncPacket.STREAM_CODEC,
            wrapHandler(RequesterSyncPacket::handle)
        );

        // client to server
        registrar.playToServer(
            RequestUpdatePacket.TYPE,
            RequestUpdatePacket.STREAM_CODEC,
            wrapHandler(RequestUpdatePacket::handle)
        );
        registrar.playToServer(
            DragAndDropPacket.TYPE,
            DragAndDropPacket.STREAM_CODEC,
            wrapHandler(DragAndDropPacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(IPayloadHandler<T> handler) {
        return (payload, context) -> context.enqueueWork(() -> handler.handle(payload, context));
    }
}
