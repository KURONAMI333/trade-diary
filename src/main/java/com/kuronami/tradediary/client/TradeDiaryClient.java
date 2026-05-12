package com.kuronami.tradediary.client;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.item.TradeDiaryItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Client-side setup: Creative tab slotting for the diary item.
 *
 * <p>No keybinding — the diary item itself is the "open" affordance
 * (right-click → open). Players asked for one less hotkey to manage,
 * and L collides with the vanilla advancement screen anyway.
 */
@EventBusSubscriber(modid = TradeDiary.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TradeDiaryClient {

    private TradeDiaryClient() {}

    /**
     * Slot the diary into the Tools & Utilities tab. Maps and writable books
     * live there too, which keeps it discoverable for players already used
     * to that page of the inventory.
     */
    @SubscribeEvent
    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TradeDiaryItems.DIARY.get());
        }
    }
}
