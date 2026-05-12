package com.kuronami.tradediary.client;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.network.TradeDiaryNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric 1.20.1 client init.
 *
 * <p>No keybinding — the diary item's right-click opens the viewer.
 * Class kept because Fabric requires the S2C raw-buf receiver to be
 * registered client-side.
 */
public final class TradeDiaryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TradeDiaryNetwork.OPEN_ID,
                (client, handler, buf, sender) -> {
                    var entries = TradeDiaryNetwork.readEntries(buf);
                    client.execute(() -> {
                        TradeClientCache.update(entries);
                        TradeClientCache.openScreenWhenReady();
                    });
                });

        TradeDiary.LOGGER.info("Trade Diary client (Fabric 1.20.1) initialized.");
    }
}
