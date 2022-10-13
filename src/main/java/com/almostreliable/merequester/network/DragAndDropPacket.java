package com.almostreliable.merequester.network;

import appeng.helpers.InventoryAction;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class DragAndDropPacket extends ClientToServerPacket<DragAndDropPacket> {

    private long requesterId;
    private int requestIndex;

    private InventoryAction action;
    private ItemStack item;

    public DragAndDropPacket(long requesterId, int requestIndex, InventoryAction action, ItemStack item) {
        this.requesterId = requesterId;
        this.requestIndex = requestIndex;
        this.action = action;
        this.item = item;
    }

    DragAndDropPacket() {}

    @Override
    public void encode(DragAndDropPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.requesterId);
        buffer.writeVarInt(packet.requestIndex);
        buffer.writeVarInt(packet.action.ordinal());
        buffer.writeItem(packet.item);
    }

    @Override
    public DragAndDropPacket decode(FriendlyByteBuf buffer) {
        return new DragAndDropPacket(
            buffer.readLong(),
            buffer.readVarInt(),
            InventoryAction.values()[buffer.readVarInt()],
            buffer.readItem()
        );
    }

    @Override
    protected void handlePacket(DragAndDropPacket packet, @Nullable ServerPlayer player) {
        if (player == null ||
            !(player.containerMenu instanceof AbstractRequesterMenu requester) ||
            packet.action != InventoryAction.SET_FILTER) {
            return;
        }
        requester.applyDragAndDrop(player, packet.requestIndex, packet.requesterId, packet.item);
    }
}
