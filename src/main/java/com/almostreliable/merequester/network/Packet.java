package com.almostreliable.merequester.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import java.util.function.Supplier;

public interface Packet<T> {
    void encode(T packet, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);

    void handle(T packet, Supplier<? extends NetworkEvent.Context> context);
}
