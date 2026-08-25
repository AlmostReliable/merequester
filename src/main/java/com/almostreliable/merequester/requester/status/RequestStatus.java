package com.almostreliable.merequester.requester.status;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import java.util.Locale;
import java.util.function.IntFunction;

public enum RequestStatus implements StringRepresentable {
    IDLE, MISSING, CPU, REQUEST, PLAN, LINK, EXPORT;

    public RequestStatus translateToClient() {
        if (this == REQUEST || this == PLAN) return IDLE;
        return this;
    }

    public boolean locksResourceSlot() {
        return this == LINK || this == EXPORT;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static final Codec<RequestStatus> CODEC = StringRepresentable.fromEnum(RequestStatus::values);
    public static final IntFunction<RequestStatus> BY_ID = ByIdMap.continuous(
        RequestStatus::ordinal,
        values(),
        ByIdMap.OutOfBoundsStrategy.ZERO
    );
    public static final StreamCodec<ByteBuf, RequestStatus> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
}
