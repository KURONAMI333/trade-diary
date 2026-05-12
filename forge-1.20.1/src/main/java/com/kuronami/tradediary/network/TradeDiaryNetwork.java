package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.client.TradeClientCache;
import com.kuronami.tradediary.data.TradeStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * SimpleChannel networking for Forge 1.20.1.
 *
 * <p>Differs from 1.21.1: uses old NetworkRegistry.newSimpleChannel + Supplier&lt;NetworkEvent.Context&gt;
 * handler signature (1.21+ moved to CustomPayloadEvent.Context direct param).
 */
public final class TradeDiaryNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TradeDiary.MODID, "main"),
            () -> PROTOCOL_VERSION,
            v -> true,
            v -> true
    );

    private TradeDiaryNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
                0,
                RequestDiaryPayload.class,
                (p, buf) -> p.encode(buf),
                RequestDiaryPayload::decode,
                TradeDiaryNetwork::handleRequest,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                1,
                OpenDiaryPayload.class,
                (p, buf) -> p.encode(buf),
                OpenDiaryPayload::decode,
                TradeDiaryNetwork::handleOpen,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    private static void handleRequest(RequestDiaryPayload payload, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp != null) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                        new OpenDiaryPayload(TradeStorage.get(sp)));
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleOpen(OpenDiaryPayload payload, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            TradeClientCache.update(payload.entries());
            TradeClientCache.openScreenWhenReady();
        }));
        ctx.setPacketHandled(true);
    }

    public static void requestFromServer() {
        CHANNEL.sendToServer(RequestDiaryPayload.INSTANCE);
    }
}
