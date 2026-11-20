package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import com.almostreliable.merequester.requester.Request;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record RequesterSyncPacket(boolean clearData, long requesterId, Payload data) implements CustomPacketPayload {

    private static final Payload EMPTY_PAYLOAD = new Payload("", 0L, 0, List.of());

    static final Type<RequesterSyncPacket> TYPE = new Type<>(Utils.getRL("requester_sync"));

    static final StreamCodec<RegistryFriendlyByteBuf, RequesterSyncPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, RequesterSyncPacket::clearData,
        ByteBufCodecs.VAR_LONG, RequesterSyncPacket::requesterId,
        Payload.PAYLOAD_STREAM_CODEC, RequesterSyncPacket::data,
        RequesterSyncPacket::new
    );

    public static RequesterSyncPacket createClearData() {
        return new RequesterSyncPacket(true, -1, EMPTY_PAYLOAD);
    }

    public static RequesterSyncPacket createInventory(
        long requesterId,
        String name,
        long sortBy,
        int requestCount,
        List<IndexedRequest> updates
    ) {
        return new RequesterSyncPacket(false, requesterId, new Payload(name, sortBy, requestCount, updates));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequesterSyncPacket payload, IPayloadContext ignoredContext) {
        if (Minecraft.getInstance().screen instanceof AbstractRequesterScreen<?> screen) {
            screen.updateFromMenu(payload.clearData, payload.requesterId, payload.data);
        }
    }

    public record Payload(String name, long sortBy, int requestCount, List<IndexedRequest> updates) {

        private static final StreamCodec<RegistryFriendlyByteBuf, Payload> PAYLOAD_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Payload::name,
            ByteBufCodecs.VAR_LONG, Payload::sortBy,
            ByteBufCodecs.VAR_INT, Payload::requestCount,
            IndexedRequest.REQUEST_STREAM_CODEC.apply(ByteBufCodecs.list()), Payload::updates,
            Payload::new
        );
    }

    public record IndexedRequest(int index, Request.Component component) {

        private static final StreamCodec<RegistryFriendlyByteBuf, IndexedRequest> REQUEST_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, IndexedRequest::index,
            Request.Component.STREAM_CODEC, IndexedRequest::component,
            IndexedRequest::new
        );
    }
}
