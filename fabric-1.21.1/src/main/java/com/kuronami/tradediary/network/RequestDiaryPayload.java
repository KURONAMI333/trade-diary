package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → Server: empty marker. */
public record RequestDiaryPayload() implements CustomPacketPayload {

    public static final RequestDiaryPayload INSTANCE = new RequestDiaryPayload();

    public static final Type<RequestDiaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TradeDiary.MODID, "request_diary"));

    public static final StreamCodec<ByteBuf, RequestDiaryPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
