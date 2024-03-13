package com.almostreliable.merequester.network;

import com.almostreliable.merequester.BuildConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

public final class PacketHandler {
    private static final String PROTOCOL = "1";

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(BuildConfig.MOD_ID).versioned(PROTOCOL);

        // server to client
        registrar.play(RequesterSyncPacket.ID, RequesterSyncPacket::decode, builder -> {
            builder.client((packet, context) -> {
                context.player().ifPresent(player -> {
                    context.workHandler().execute(() -> {
                        packet.handlePacket(player);
                    });
                });
            });
        });

        // client to server
        registrar.play(RequestUpdatePacket.ID, RequestUpdatePacket::decode, builder -> {
            builder.server((packet, context) -> {
                context.player().ifPresent(player -> {
                    context.workHandler().execute(() -> {
                        packet.handlePacket(player);
                    });
                });
            });
        });
        registrar.play(DragAndDropPacket.ID, DragAndDropPacket::decode, builder -> {
            builder.server((packet, context) -> {
                context.player().ifPresent(player -> {
                    context.workHandler().execute(() -> packet.handlePacket(player));
                });
            });
        });
    }
}
