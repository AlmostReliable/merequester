package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class RequestUpdatePacket implements Packet {

    static final ResourceLocation ID = Utils.getRL("request_update");

    private final UpdateType updateType;
    private final long requesterId;
    private final int requestIndex;

    private boolean state;
    private long amount;
    private long batch;

    public RequestUpdatePacket(long requesterId, int requestIndex, boolean state) {
        this.updateType = UpdateType.STATE;
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.state = state;
    }

    public RequestUpdatePacket(long requesterId, int requestIndex, long amount, long batch) {
        this.updateType = UpdateType.NUMBERS;
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.amount = amount;
        this.batch = batch;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(requesterId);
        buffer.writeVarInt(requestIndex);

        buffer.writeVarInt(updateType.ordinal());
        if (updateType == UpdateType.STATE) {
            buffer.writeBoolean(state);
        } else if (updateType == UpdateType.NUMBERS) {
            buffer.writeLong(amount);
            buffer.writeLong(batch);
        } else {
            throw new IllegalStateException("Unknown update type: " + updateType);
        }
    }

    static RequestUpdatePacket decode(FriendlyByteBuf buffer) {
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
    public void handle(Player player) {
        if (player.containerMenu instanceof AbstractRequesterMenu requester) {
            if (updateType == UpdateType.STATE) {
                requester.updateRequesterState(requesterId, requestIndex, state);
            } else if (updateType == UpdateType.NUMBERS) {
                requester.updateRequesterNumbers(requesterId, requestIndex, amount, batch);
            }
        }
    }

    private enum UpdateType {
        STATE, NUMBERS
    }
}
