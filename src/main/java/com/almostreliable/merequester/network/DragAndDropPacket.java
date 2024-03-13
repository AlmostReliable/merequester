package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DragAndDropPacket implements CustomPacketPayload {

    public static final ResourceLocation ID = Utils.getRL("drag_and_drop");

    private long requesterId;
    private int requestIndex;

    private ItemStack item;

    public DragAndDropPacket(long requesterId, int requestIndex, ItemStack item) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.item = item;
    }

    DragAndDropPacket() {}

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

    public static DragAndDropPacket decode(FriendlyByteBuf buffer) {
        return new DragAndDropPacket(
            buffer.readLong(),
            buffer.readVarInt(),
            buffer.readItem()
        );
    }

    public void handlePacket(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.containerMenu instanceof AbstractRequesterMenu requester) {
            requester.applyDragAndDrop(serverPlayer, requestIndex, requesterId, item);
        }
    }
}

