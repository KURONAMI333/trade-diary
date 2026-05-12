package com.kuronami.tradediary.item;

import com.kuronami.tradediary.TradeDiary;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry for Trade Diary.
 *
 * <p>Currently a single entry: the {@link DiaryItem diary} that opens the
 * diary viewer when right-clicked. Kept as its own class so the registration
 * surface stays compact and {@link TradeDiary} only knows to forward a bus.
 */
public final class TradeDiaryItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TradeDiary.MODID);

    /**
     * The Journal — right-click to open the diary screen. Stacks to 1 because
     * each one is conceptually "your" notebook; duplicates would just be confusing.
     */
    public static final DeferredItem<DiaryItem> DIARY = ITEMS.register("diary",
            () -> new DiaryItem(new Item.Properties().stacksTo(1)));

    private TradeDiaryItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
