package com.almostreliable.merequester.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface Packet extends CustomPacketPayload {

    void handle(Player player);
}
