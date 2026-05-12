package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.data.TradeEntry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric 1.20.1 raw byte-buf networking — 16-field encoding to match TradeEntry.
 *
 * <p>1.20.1's Fabric API predates PayloadTypeRegistry, so we hand-encode each
 * field via {@link FriendlyByteBuf}. Two channels: REQUEST (C2S, empty body)
 * and OPEN (S2C, list of entries).
 */
public final class TradeDiaryNetwork {

    public static final ResourceLocation REQUEST_ID =
            new ResourceLocation(TradeDiary.MODID, "request_diary");
    public static final ResourceLocation OPEN_ID =
            new ResourceLocation(TradeDiary.MODID, "open_diary");

    private TradeDiaryNetwork() {}

    public static void sendDiaryToPlayer(ServerPlayer player, List<TradeEntry> entries) {
        FriendlyByteBuf buf = PacketByteBufs.create();
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
        ServerPlayNetworking.send(player, OPEN_ID, buf);
    }

    public static List<TradeEntry> readEntries(FriendlyByteBuf buf) {
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
        return list;
    }
}
