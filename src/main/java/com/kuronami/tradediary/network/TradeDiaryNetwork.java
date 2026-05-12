package com.kuronami.tradediary.network;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.client.TradeClientCache;
import com.kuronami.tradediary.data.TradeStorage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the Trade Diary packet types and routes incoming payloads to handlers.
 *
 * <p>Server → Client: {@link OpenDiaryPayload} carries the trade list,
 * client caches it and opens the screen.
 *
 * <p>Client → Server: {@link RequestDiaryPayload} asks the server to send
 * the player's diary entries.
 */
@EventBusSubscriber(modid = TradeDiary.MODID)
public final class TradeDiaryNetwork {

    /** Bumped when the wire format changes. */
    private static final String VERSION = "1";

    private TradeDiaryNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.playToClient(
                OpenDiaryPayload.TYPE,
                OpenDiaryPayload.STREAM_CODEC,
                TradeDiaryNetwork::handleOpenDiaryClient);

        registrar.playToServer(
                RequestDiaryPayload.TYPE,
                RequestDiaryPayload.STREAM_CODEC,
                TradeDiaryNetwork::handleRequestDiaryServer);
    }

    private static void handleRequestDiaryServer(RequestDiaryPayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        var entries = TradeStorage.get(sp);
        ctx.reply(new OpenDiaryPayload(entries));
    }

    private static void handleOpenDiaryClient(OpenDiaryPayload payload, IPayloadContext ctx) {
        TradeClientCache.update(payload.entries());
        TradeClientCache.openScreenWhenReady();
    }
}
