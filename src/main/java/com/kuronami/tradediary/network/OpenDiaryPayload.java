package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.data.TradeEntry;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server → Client payload carrying the player's full diary.
 *
 * <p>Sent on demand when the client requests to open the Trade Diary screen.
 * Re-sent on each open (diary is small enough — usually &lt; 200 entries —
 * that incremental sync isn't worth the complexity).
 */
public record OpenDiaryPayload(List<TradeEntry> entries) implements CustomPacketPayload {

    public static final Type<OpenDiaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TradeDiary.MODID, "open_diary"));

    public static final StreamCodec<ByteBuf, OpenDiaryPayload> STREAM_CODEC = ByteBufCodecs
            .fromCodec(TradeEntry.CODEC.listOf())
            .map(OpenDiaryPayload::new, OpenDiaryPayload::entries);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
