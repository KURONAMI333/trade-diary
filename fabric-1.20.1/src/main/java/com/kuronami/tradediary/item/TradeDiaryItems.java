package com.kuronami.tradediary.item;

import com.kuronami.tradediary.TradeDiary;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Item registration for Trade Diary (Fabric 1.21.1).
 *
 * <p>Mirrors the NeoForge registration but uses Fabric's direct registry calls
 * + ItemGroupEvents for the Creative tab. Same item id, same texture, same
 * stack-size-1 properties, same right-click behavior — the differences are
 * entirely in the registration plumbing.
 */
public final class TradeDiaryItems {

    public static final Item DIARY = Registry.register(
            BuiltInRegistries.ITEM,
            new ResourceLocation(TradeDiary.MODID, "diary"),
            new DiaryItem(new Item.Properties().stacksTo(1))
    );

    private TradeDiaryItems() {}

    /** Called from the ModInitializer; forces class load + tab registration. */
    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(content -> content.accept(DIARY));
    }
}
