package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DragAndDropPacket implements Packet {

    static final ResourceLocation ID = Utils.getRL("drag_and_drop");

    private final long requesterId;
    private final int requestIndex;
    private final ItemStack item;

    public DragAndDropPacket(long requesterId, int requestIndex, ItemStack item) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.item = item;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(requesterId);
        buffer.writeVarInt(requestIndex);
        buffer.writeItem(item);
    }

    static DragAndDropPacket decode(FriendlyByteBuf buffer) {
        return new DragAndDropPacket(buffer.readLong(), buffer.readVarInt(), buffer.readItem());
    }

    @Override
    public void handle(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.containerMenu instanceof AbstractRequesterMenu requester) {
            requester.applyDragAndDrop(serverPlayer, requestIndex, requesterId, item);
        }
    }
}
