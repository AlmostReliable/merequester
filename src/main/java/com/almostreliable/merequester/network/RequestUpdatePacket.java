package com.almostreliable.merequester.network;

import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class RequestUpdatePacket extends ClientToServerPacket<RequestUpdatePacket> {

    private long requesterId;
    private int requestIndex;

    private boolean state;
    private long count;
    private long batch;

    private UpdateType updateType;

    public RequestUpdatePacket(long requesterId, int requestIndex, boolean state) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.state = state;
        this.updateType = UpdateType.STATE;
    }

    public RequestUpdatePacket(long requesterId, int requestIndex, long count, long batch) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.count = count;
        this.batch = batch;
        this.updateType = UpdateType.NUMBERS;
    }

    RequestUpdatePacket() {}

    @Override
    public void encode(RequestUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requesterId);
        buffer.writeVarInt(packet.requestIndex);

        buffer.writeVarInt(packet.updateType.ordinal());
        if (packet.updateType == UpdateType.STATE) {
            buffer.writeBoolean(packet.state);
        } else if (packet.updateType == UpdateType.NUMBERS) {
            buffer.writeLong(packet.count);
            buffer.writeLong(packet.batch);
        } else {
            throw new IllegalStateException("Unknown update type: " + packet.updateType);
        }
    }

    @Override
    public RequestUpdatePacket decode(FriendlyByteBuf buffer) {
        var id = buffer.readLong();
        var index = buffer.readVarInt();

        var type = UpdateType.values()[buffer.readVarInt()];
        if (type == UpdateType.STATE) {
            return new RequestUpdatePacket(id, index, buffer.readBoolean());
        }
        if (type == UpdateType.NUMBERS) {
            return new RequestUpdatePacket(id, index, buffer.readLong(), buffer.readLong());
        }
        throw new IllegalStateException("Unknown update type: " + type);
    }

    @Override
    protected void handlePacket(RequestUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof RequesterTerminalMenu terminal) {
            if (packet.updateType == UpdateType.STATE) {
                terminal.updateRequesterState(packet.requesterId, packet.requestIndex, packet.state);
            } else if (packet.updateType == UpdateType.NUMBERS) {
                terminal.updateRequesterNumbers(packet.requesterId, packet.requestIndex, packet.count, packet.batch);
            }
        }
    }

    private enum UpdateType {
        STATE,
        NUMBERS
    }
}
