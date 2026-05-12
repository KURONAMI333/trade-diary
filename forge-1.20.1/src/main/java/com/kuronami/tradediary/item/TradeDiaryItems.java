package com.kuronami.tradediary.item;

import com.kuronami.tradediary.TradeDiary;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Item registry for Trade Diary (Forge 1.20.1).
 *
 * <p>Uses Forge's DeferredRegister + RegistryObject pattern. The Creative
 * tab hookup uses BuildCreativeModeTabContentsEvent on the mod bus.
 */
@Mod.EventBusSubscriber(modid = TradeDiary.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TradeDiaryItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TradeDiary.MODID);

    public static final RegistryObject<Item> DIARY = ITEMS.register("diary",
            () -> new DiaryItem(new Item.Properties().stacksTo(1)));

    private TradeDiaryItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    @SubscribeEvent
    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DIARY.get());
        }
    }
}
