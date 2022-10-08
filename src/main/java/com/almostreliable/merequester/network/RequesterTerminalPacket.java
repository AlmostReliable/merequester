package com.almostreliable.merequester.network;

import com.almostreliable.merequester.terminal.RequesterTerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class RequesterTerminalPacket extends ServerToClientPacket<RequesterTerminalPacket> {

    private boolean clearExistingData;
    private long inventoryId;
    private CompoundTag in;

    private RequesterTerminalPacket(boolean clearExistingData, long inventoryId, CompoundTag in) {
        this.clearExistingData = clearExistingData;
        this.inventoryId = inventoryId;
        this.in = in;
    }

    RequesterTerminalPacket() {}

    @Override
    public void encode(RequesterTerminalPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.clearExistingData);
        buffer.writeLong(packet.inventoryId);
        buffer.writeNbt(packet.in);
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
            screen.postInventoryUpdate(packet.clearExistingData, packet.inventoryId, packet.in);
        }
    }

    public static RequesterTerminalPacket clearExistingData() {
        return new RequesterTerminalPacket(true, -1, new CompoundTag());
    }

    public static RequesterTerminalPacket inventory(long id, CompoundTag data) {
        return new RequesterTerminalPacket(false, id, data);
    }
}
