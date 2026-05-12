package com.kuronami.tradediary;

import com.kuronami.tradediary.data.TradeStorage;
import com.kuronami.tradediary.item.TradeDiaryItems;
import com.kuronami.tradediary.network.TradeDiaryNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Trade Diary — Fabric 1.20.1 entry point.
 *
 * <p>Trades are captured by Mixin (see {@code mixin/AbstractVillagerMixin}).
 * Networking uses the older Fabric API (no PayloadTypeRegistry yet);
 * raw {@code FriendlyByteBuf} round-trips via {@link ServerPlayNetworking}.
 */
public final class TradeDiaryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TradeDiaryItems.init();

        ServerPlayNetworking.registerGlobalReceiver(
                TradeDiaryNetwork.REQUEST_ID,
                (server, player, handler, buf, sender) -> {
                    server.execute(() -> TradeDiaryNetwork.sendDiaryToPlayer(player,
                            TradeStorage.get(player)));
                });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TradeStorage.invalidate());

        TradeDiary.LOGGER.info("Trade Diary (Fabric 1.20.1) initialized.");
    }
}
