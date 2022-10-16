package com.almostreliable.merequester.network;

import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.requester.abstraction.AbstractRequesterMenu;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public final class DragAndDropPacket {

    public static final ResourceLocation CHANNEL = Utils.getRL("drag_and_drop");

    private DragAndDropPacket() {}

    @SuppressWarnings("unused")
    static void handlePacket(
        MinecraftServer server, ServerPlayer player,
        ServerGamePacketListenerImpl packetListener, FriendlyByteBuf buffer,
        PacketSender packetSender
    ) {
        if (player.containerMenu instanceof AbstractRequesterMenu requester) {
            var requesterId = buffer.readLong();
            var requestIndex = buffer.readVarInt();
            var item = buffer.readItem();
            requester.applyDragAndDrop(player, requestIndex, requesterId, item);
        }
    }

    public static FriendlyByteBuf encode(long requesterId, int requestIndex, ItemStack item) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeLong(requesterId);
        buffer.writeVarInt(requestIndex);
        buffer.writeItem(item);
        return buffer;
    }
}
