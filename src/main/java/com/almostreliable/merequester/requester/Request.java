package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.requester.abstraction.RequestHost;
import com.almostreliable.merequester.requester.status.RequestStatus;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class Request implements INBTSerializable<CompoundTag> {

    // serialization IDs
    private static final String STATE_ID = "state";
    private static final String KEY_ID = "key";
    private static final String AMOUNT_ID = "amount";
    private static final String BATCH_ID = "batch";
    private static final String STATUS_ID = "status";

    @Nullable
    private final RequestHost host;
    private final int index;

    private boolean state = true;
    @Nullable
    private AEKey key;
    private long amount;
    private long batch = 1;

    // this status is only relevant for the client
    // the actual request status is stored in the BlockEntity
    private RequestStatus clientStatus = RequestStatus.IDLE;

    Request(@Nullable RequestHost host, int index) {
        this.host = host;
        this.index = index;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        tag.putBoolean(STATE_ID, state);
        if (key != null) tag.put(KEY_ID, key.toTagGeneric(registries));
        tag.putLong(AMOUNT_ID, amount);
        tag.putLong(BATCH_ID, batch);
        tag.putInt(STATUS_ID, clientStatus.ordinal());
        return tag;
    }

    public Component toComponent() {
        return new Component(
            state,
            Optional.ofNullable(key),
            amount,
            batch,
            clientStatus
        );
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        state = tag.getBoolean(STATE_ID);
        key = tag.contains(KEY_ID) ? AEKey.fromTagGeneric(registries, tag.getCompound(KEY_ID)) : null;
        amount = tag.getLong(AMOUNT_ID);
        batch = tag.getLong(BATCH_ID);
        clientStatus = RequestStatus.values()[tag.getInt(STATUS_ID)];
    }

    public void fromComponent(Component request) {
        state = request.state();
        key = request.key().orElse(null);
        amount = request.amount();
        batch = request.batch();
        clientStatus = request.clientStatus();
    }

    public void updateState(boolean state) {
        if (this.state != state) {
            this.state = state;
            if (host != null) host.saveChanges();
        }
    }

    public void updateAmount(long amount) {
        if (amount <= 0 && clientStatus.locksResourceSlot()) return;
        if (key == null || amount <= 0) {
            resetSlot();
            return;
        }
        if (this.amount != amount) {
            this.amount = amount;
            if (host != null) host.saveChanges();
        }
    }

    public void updateBatch(long batch) {
        var oldBatch = this.batch;
        this.batch = Mth.clamp(batch, 1, batch);
        if (oldBatch != this.batch && host != null) host.saveChanges();
    }

    @Override
    public String toString() {
        return String.format(
            "Request[state=%s, key=%s, amount=%s, batch=%s, client_status=%s]",
            state,
            key == null ? "none" : key.getDisplayName(),
            amount,
            batch,
            clientStatus
        );
    }

    public boolean isDifferent(Request clientRequest) {
        return state != clientRequest.state || !Objects.equals(key, clientRequest.key) || amount != clientRequest.amount ||
            batch != clientRequest.batch || clientStatus != clientRequest.clientStatus;
    }

    @Nullable
    GenericStack toGenericStack() {
        if (key == null) return null;
        return new GenericStack(key, amount);
    }

    void updateKey(@Nullable GenericStack stack) {
        if (stack == null) {
            if (key != null) resetSlot();
            return;
        }
        if (key != null && key.matches(stack)) {
            if (amount != stack.amount()) updateAmount(stack.amount());
            return;
        }
        key = stack.what();
        amount = stack.amount();
        batch = stack.what().getAmountPerUnit();
        keyChanged();
    }

    void setClientKey(AEKey key, long amount) {
        this.key = key;
        this.amount = amount;
    }

    private void keyChanged() {
        if (host != null) host.requestChanged(index);
    }

    private void resetSlot() {
        if (key == null && amount == 0) return;
        key = null;
        amount = 0;
        batch = 1;
        keyChanged();
    }

    public int getIndex() {
        return index;
    }

    public boolean getState() {
        return state;
    }

    @Nullable
    public AEKey getKey() {
        return key;
    }

    public long getAmount() {
        return amount;
    }

    public long getBatch() {
        return batch;
    }

    @OnlyIn(Dist.CLIENT)
    public RequestHost getRequesterReference() {
        assert host != null;
        return host;
    }

    public RequestStatus getClientStatus() {
        return clientStatus;
    }

    void setClientStatus(RequestStatus clientStatus) {
        this.clientStatus = clientStatus;
    }

    public boolean isRequesting() {
        return state && key != null;
    }

    public record Component(
        boolean state,
        Optional<AEKey> key,
        long amount,
        long batch,
        RequestStatus clientStatus
    ) {

        public static final Codec<Component> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.fieldOf("state").forGetter(Component::state),
            Codec.optionalField("key", AEKey.CODEC, false).forGetter(Component::key),
            Codec.LONG.fieldOf("amount").forGetter(Component::amount),
            Codec.LONG.fieldOf("batch").forGetter(Component::batch),
            RequestStatus.CODEC.fieldOf("client_status").forGetter(Component::clientStatus)
        ).apply(builder, Component::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Component::state,
            ByteBufCodecs.optional(AEKey.STREAM_CODEC), Component::key,
            ByteBufCodecs.VAR_LONG, Component::amount,
            ByteBufCodecs.VAR_LONG, Component::batch,
            RequestStatus.STREAM_CODEC, Component::clientStatus,
            Component::new
        );
    }
}
