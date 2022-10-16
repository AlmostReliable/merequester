package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class RequesterSyncPacket {

    public static final ResourceLocation CHANNEL = Utils.getRL("requester_sync");

    private RequesterSyncPacket() {}

    @SuppressWarnings("unused")
    static void handlePacket(
        Minecraft mc, ClientPacketListener packetListener, FriendlyByteBuf buffer, PacketSender packetSender
    ) {
        var clearData = buffer.readBoolean();
        var requesterId = buffer.readLong();
        var data = Objects.requireNonNull(buffer.readNbt());
        mc.execute(() -> {
            if (mc.screen instanceof AbstractRequesterScreen<?> screen) {
                screen.updateFromMenu(clearData, requesterId, data);
            }
        });
    }

    public static FriendlyByteBuf encode() {
        return encode(true, -1, new CompoundTag());
    }

    public static FriendlyByteBuf encode(long requesterId, CompoundTag data) {
        return encode(false, requesterId, data);
    }

    private static FriendlyByteBuf encode(boolean clearData, long requesterId, CompoundTag data) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBoolean(clearData);
        buffer.writeLong(requesterId);
        buffer.writeNbt(data);
        return buffer;
    }
}
