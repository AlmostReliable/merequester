package com.almostreliable.merequester.network;

import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class RequestStatePacket extends ClientToServerPacket<RequestStatePacket> {

    private long requesterId;
    private int requestIndex;
    private boolean state;

    public RequestStatePacket(long requesterId, int requestIndex, boolean state) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.state = state;
    }

    public RequestStatePacket() {}

    @Override
    public void encode(RequestStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requesterId);
        buffer.writeVarInt(packet.requestIndex);
        buffer.writeBoolean(packet.state);
    }

    @Override
    public RequestStatePacket decode(FriendlyByteBuf buffer) {
        return new RequestStatePacket(buffer.readLong(), buffer.readVarInt(), buffer.readBoolean());
    }

    @Override
    protected void handlePacket(RequestStatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof RequesterTerminalMenu terminal) {
            terminal.updateRequesterState(packet.requesterId, packet.requestIndex, packet.state);
        }
    }
}
