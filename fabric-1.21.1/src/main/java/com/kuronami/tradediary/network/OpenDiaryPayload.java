package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.data.TradeEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Server → Client: diary list (Fabric 1.21.1). */
public record OpenDiaryPayload(List<TradeEntry> entries) implements CustomPacketPayload {

    public static final Type<OpenDiaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TradeDiary.MODID, "open_diary"));

    public static final StreamCodec<ByteBuf, OpenDiaryPayload> STREAM_CODEC = ByteBufCodecs
            .fromCodec(TradeEntry.CODEC.listOf())
            .map(OpenDiaryPayload::new, OpenDiaryPayload::entries);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
