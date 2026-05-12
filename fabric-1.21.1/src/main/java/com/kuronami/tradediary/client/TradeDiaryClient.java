package com.kuronami.tradediary.client;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.network.OpenDiaryPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric 1.21.1 client init.
 *
 * <p>No keybinding — the diary item's right-click opens the viewer.
 * L collides with the advancement screen, so we leave hotkey assignment
 * to the player via the standard Controls menu if they want one.
 *
 * <p>This class still exists because Fabric requires the S2C payload
 * receiver to be registered client-side.
 */
public final class TradeDiaryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenDiaryPayload.TYPE, (payload, ctx) -> {
            TradeClientCache.update(payload.entries());
            TradeClientCache.openScreenWhenReady();
        });

        TradeDiary.LOGGER.info("Trade Diary client (Fabric 1.21.1) initialized.");
    }
}
