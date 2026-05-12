package com.kuronami.tradediary.mixin;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric 1.20.1: hook into {@link AbstractVillager#notifyTrade} to capture
 * villager trades. Same approach as Fabric 1.21.1 — Fabric has no first-class
 * TradeWithVillagerEvent, so we mixin where vanilla itself fires its "trade
 * just happened" callback.
 *
 * <p>The 1.20.1 vanilla signatures match 1.21.1 here (AbstractVillager hasn't
 * changed shape for these methods), so the mixin body is identical between
 * the two version forks.
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @Inject(method = "notifyTrade", at = @At("HEAD"))
    private void tradediary$onNotifyTrade(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager self = (AbstractVillager) (Object) this;
        Player tradingPlayer = self.getTradingPlayer();
        if (!(tradingPlayer instanceof ServerPlayer player)) return;

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

        String profession = "minecraft:none";
        int profLevel = 0;
        if (self instanceof Villager villager) {
            VillagerData vd = villager.getVillagerData();
            VillagerProfession prof = vd.getProfession();
            ResourceLocation profKey = BuiltInRegistries.VILLAGER_PROFESSION.getKey(prof);
            if (profKey != null) profession = profKey.toString();
            profLevel = vd.getLevel();
        } else {
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
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
                self.getUUID().toString(),
                profession,
                profLevel,
                idOf(costA),  costA.getCount(),
                idOf(costB),  costB.getCount(),
                idOf(result), result.getCount()
        );

        TradeDiary.LOGGER.info(
                "Trade recorded: {}x{} <-> {}x{} from {} (lvl {}) at {}",
                idOf(costA), costA.getCount(),
                idOf(result), result.getCount(),
                profession, profLevel, pos
        );
        TradeStorage.appendOrMerge(player, entry, 5_000L);
    }

    private static String idOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }
}
