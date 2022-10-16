package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class RequestUpdatePacket {

    public static final ResourceLocation CHANNEL = Utils.getRL("request_update");

    private RequestUpdatePacket() {}

    @SuppressWarnings("unused")
    static void handlePacket(
        MinecraftServer server, ServerPlayer player,
        ServerGamePacketListenerImpl packetListener, FriendlyByteBuf buffer,
        PacketSender packetSender
    ) {
        if (player.containerMenu instanceof AbstractRequesterMenu requester) {
            var requesterId = buffer.readLong();
            var requestIndex = buffer.readVarInt();
            var updateType = UpdateType.values()[buffer.readVarInt()];
            if (updateType == UpdateType.STATE) {
                var state = buffer.readBoolean();
                requester.updateRequesterState(requesterId, requestIndex, state);
            } else if (updateType == UpdateType.NUMBERS) {
                var amount = buffer.readLong();
                var batch = buffer.readLong();
                requester.updateRequesterNumbers(requesterId, requestIndex, amount, batch);
            }
        }
    }

    public static FriendlyByteBuf encode(long requesterId, int requestIndex, boolean state) {
        var buffer = encode(requesterId, requestIndex);
        buffer.writeVarInt(UpdateType.STATE.ordinal());
        buffer.writeBoolean(state);
        return buffer;
    }

    public static FriendlyByteBuf encode(long requesterId, int requestIndex, long amount, long batch) {
        var buffer = encode(requesterId, requestIndex);
        buffer.writeVarInt(UpdateType.NUMBERS.ordinal());
        buffer.writeLong(amount);
        buffer.writeLong(batch);
        return buffer;
    }

    private static FriendlyByteBuf encode(long requesterId, int requestIndex) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeLong(requesterId);
        buffer.writeVarInt(requestIndex);
        return buffer;
    }

    private enum UpdateType {
        STATE,
        NUMBERS
    }
}
