package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.client.abstraction.AbstractRequesterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class RequesterSyncPacket implements Packet {

    static final ResourceLocation ID = Utils.getRL("requester_sync");

    private final boolean clearData;
    private final long requesterId;
    private final CompoundTag data;

    private RequesterSyncPacket(boolean clearData, long requesterId, CompoundTag data) {
        this.clearData = clearData;
        this.requesterId = requesterId;
        this.data = data;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static RequesterSyncPacket clearData() {
        return new RequesterSyncPacket(true, -1, new CompoundTag());
    }

    public static RequesterSyncPacket inventory(long requesterId, CompoundTag data) {
        return new RequesterSyncPacket(false, requesterId, data);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(clearData);
        buffer.writeLong(requesterId);
        buffer.writeNbt(data);
    }

    static RequesterSyncPacket decode(FriendlyByteBuf buffer) {
        return new RequesterSyncPacket(buffer.readBoolean(), buffer.readLong(), Objects.requireNonNull(buffer.readNbt()));
    }

    @Override
    public void handle(Player player) {
        if (Minecraft.getInstance().screen instanceof AbstractRequesterScreen<?> screen) {
            screen.updateFromMenu(clearData, requesterId, data);
        }
    }
}
