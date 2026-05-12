package com.kuronami.tradediary;

import com.kuronami.tradediary.data.TradeStorage;
import com.kuronami.tradediary.item.TradeDiaryItems;
import com.kuronami.tradediary.network.OpenDiaryPayload;
import com.kuronami.tradediary.network.RequestDiaryPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Trade Diary — Fabric 1.21.1 entry point.
 *
 * <p>Fabric has no first-class TradeWithVillagerEvent; we mixin
 * {@code AbstractVillager#notifyTrade} instead (see
 * {@code mixin/AbstractVillagerMixin}).
 *
 * <p>Networking uses the Fabric API's PayloadTypeRegistry / ServerPlayNetworking.
 * Cache is invalidated on server stop so leaving one world and entering another
 * doesn't show stale entries from the previous one.
 */
public final class TradeDiaryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TradeDiaryItems.init();

        PayloadTypeRegistry.playC2S().register(RequestDiaryPayload.TYPE, RequestDiaryPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDiaryPayload.TYPE, OpenDiaryPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestDiaryPayload.TYPE, (payload, ctx) -> {
            var player = ctx.player();
            ServerPlayNetworking.send(player, new OpenDiaryPayload(TradeStorage.get(player)));
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TradeStorage.invalidate());

        TradeDiary.LOGGER.info("Trade Diary (Fabric 1.21.1) initialized.");
    }
}
