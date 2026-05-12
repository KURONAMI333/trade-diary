package com.kuronami.tradediary.network;

import com.kuronami.tradediary.data.TradeEntry;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * S2C payload — full diary snapshot. 16 fields per entry, fixed order; bump
 * {@code TradeDiaryNetwork.PROTOCOL_VERSION} if you change the format.
 */
public record OpenDiaryPayload(List<TradeEntry> entries) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (TradeEntry e : entries) {
            buf.writeLong(e.epochMillis());
            buf.writeLong(e.worldDay());
            buf.writeUtf(e.dimensionId());
            buf.writeUtf(e.biomeId());
            buf.writeInt(e.x()); buf.writeInt(e.y()); buf.writeInt(e.z());
            buf.writeUtf(e.villagerUuid());
            buf.writeUtf(e.profession());
            buf.writeVarInt(e.professionLevel());
            buf.writeUtf(e.costAItem());
            buf.writeVarInt(e.costACount());
            buf.writeUtf(e.costBItem());
            buf.writeVarInt(e.costBCount());
            buf.writeUtf(e.resultItem());
            buf.writeVarInt(e.resultCount());
        }
    }

    public static OpenDiaryPayload decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<TradeEntry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new TradeEntry(
                    buf.readLong(), buf.readLong(),
                    buf.readUtf(), buf.readUtf(),
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readUtf(),
                    buf.readUtf(), buf.readVarInt(),
                    buf.readUtf(), buf.readVarInt(),
                    buf.readUtf(), buf.readVarInt(),
                    buf.readUtf(), buf.readVarInt()));
        }
        return new OpenDiaryPayload(list);
    }
}
