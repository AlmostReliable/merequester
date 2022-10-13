package com.almostreliable.merequester.network;

import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class RequesterSyncPacket extends ServerToClientPacket<RequesterSyncPacket> {

    private boolean clearData;
    private long requesterId;
    private CompoundTag data;

    private RequesterSyncPacket(boolean clearData, long requesterId, CompoundTag data) {
        this.clearData = clearData;
        this.requesterId = requesterId;
        this.data = data;
    }

    RequesterSyncPacket() {}

    public static RequesterSyncPacket clearData() {
        return new RequesterSyncPacket(true, -1, new CompoundTag());
    }

    public static RequesterSyncPacket inventory(long requesterId, CompoundTag data) {
        return new RequesterSyncPacket(false, requesterId, data);
    }

    @Override
    public void encode(RequesterSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.clearData);
        buffer.writeLong(packet.requesterId);
        buffer.writeNbt(packet.data);
    }

    @Override
    public RequesterSyncPacket decode(FriendlyByteBuf buffer) {
        return new RequesterSyncPacket(
            buffer.readBoolean(),
            buffer.readLong(),
            Objects.requireNonNull(buffer.readNbt())
        );
    }

    @Override
    protected void handlePacket(RequesterSyncPacket packet, ClientLevel level) {
        if (Minecraft.getInstance().screen instanceof AbstractRequesterScreen<?> screen) {
            screen.updateFromMenu(packet.clearData, packet.requesterId, packet.data);
        }
    }
}
