package com.kuronami.tradediary.event;

import com.kuronami.tradediary.TradeDiary;
import com.kuronami.tradediary.data.TradeEntry;
import com.kuronami.tradediary.data.TradeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Listens for villager trades and persists a {@link TradeEntry}.
 *
 * <p>{@code TradeWithVillagerEvent} fires server-side every time a trade
 * completes (vanilla {@code AbstractVillager#processTrade}). The event hands
 * us the {@link MerchantOffer} the player took and the {@link AbstractVillager}
 * instance — enough to capture profession, level, cost, result, and identity.
 */
@EventBusSubscriber(modid = TradeDiary.MODID)
public final class TradeListener {

    private TradeListener() {}

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MerchantOffer offer = event.getMerchantOffer();
        AbstractVillager merchant = event.getAbstractVillager();

        BlockPos pos = player.blockPosition();
        String biomeId = "";
        try {
            Holder<Biome> biomeHolder = player.level().getBiome(pos);
            biomeId = biomeHolder.unwrapKey()
                    .map(k -> k.location().toString())
                    .orElse("");
        } catch (Exception ex) {
            TradeDiary.LOGGER.debug("Biome resolution skipped at {}: {}", pos, ex.toString());
        }

        // Profession + level only meaningful for proper villagers; wandering
        // traders share the merchant interface but have no profession.
        String profession = "minecraft:none";
        int profLevel = 0;
        if (merchant instanceof Villager villager) {
            VillagerData vd = villager.getVillagerData();
            VillagerProfession prof = vd.getProfession();
            ResourceLocation profKey = BuiltInRegistries.VILLAGER_PROFESSION.getKey(prof);
            if (profKey != null) profession = profKey.toString();
            profLevel = vd.getLevel();
        } else {
            // Wandering trader / etc.
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(merchant.getType());
            if (entityKey != null) profession = entityKey.toString();
        }

        ItemStack costA = offer.getBaseCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();

        TradeEntry entry = new TradeEntry(
                System.currentTimeMillis(),
                player.level().getDayTime() / 24000L,
                player.level().dimension().location().toString(),
                biomeId,
                pos.getX(), pos.getY(), pos.getZ(),
                merchant.getUUID().toString(),
                profession,
                profLevel,
                idOf(costA),  costA.getCount(),
                idOf(costB),  costB.getCount(),
                idOf(result), result.getCount()
        );

        TradeDiary.LOGGER.info(
                "Trade recorded: {}×{} ↔ {}×{} from {} (lvl {}) at {}",
                idOf(costA), costA.getCount(),
                idOf(result), result.getCount(),
                profession, profLevel, pos
        );
        // 5-second merge window collapses shift-click bursts into one entry.
        TradeStorage.appendOrMerge(player, entry, 5_000L);
    }

    private static String idOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    /**
     * Clear the in-memory cache when the integrated server stops (single-player
     * world exit). Without this, loading a different world reuses the previous
     * world's cached entries until the player relogs.
     */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TradeStorage.invalidate();
    }
}
