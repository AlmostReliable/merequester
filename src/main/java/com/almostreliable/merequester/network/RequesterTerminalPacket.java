package com.almostreliable.merequester.network;

import com.almostreliable.merequester.terminal.RequesterTerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class RequesterTerminalPacket extends ServerToClientPacket<RequesterTerminalPacket> {

    private boolean clearData;
    private long requesterId;
    private CompoundTag data;

    private RequesterTerminalPacket(boolean clearData, long requesterId, CompoundTag data) {
        this.clearData = clearData;
        this.requesterId = requesterId;
        this.data = data;
    }

    RequesterTerminalPacket() {}

    public static RequesterTerminalPacket clearData() {
        return new RequesterTerminalPacket(true, -1, new CompoundTag());
    }

    public static RequesterTerminalPacket inventory(long requesterId, CompoundTag data) {
        return new RequesterTerminalPacket(false, requesterId, data);
    }

    @Override
    public void encode(RequesterTerminalPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.clearData);
        buffer.writeLong(packet.requesterId);
        buffer.writeNbt(packet.data);
    }

    @Override
    public RequesterTerminalPacket decode(FriendlyByteBuf buffer) {
        return new RequesterTerminalPacket(
            buffer.readBoolean(),
            buffer.readLong(),
            Objects.requireNonNull(buffer.readNbt())
        );
    }

    @Override
    protected void handlePacket(RequesterTerminalPacket packet, ClientLevel level) {
        if (Minecraft.getInstance().screen instanceof RequesterTerminalScreen screen) {
            screen.updateFromMenu(packet.clearData, packet.requesterId, packet.data);
        }
    }
}
