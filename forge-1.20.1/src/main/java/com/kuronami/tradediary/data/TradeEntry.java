package com.kuronami.tradediary.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Immutable record of a single villager trade.
 *
 * <p>One entry is appended the moment a villager trade completes. Capped at 16
 * fields because DataFixerUpper's {@link RecordCodecBuilder#group} maxes out
 * there. (Custom villager names were the field that got dropped to fit.)
 */
public record TradeEntry(
        long epochMillis,
        long worldDay,
        String dimensionId,
        String biomeId,
        int x,
        int y,
        int z,
        String villagerUuid,
        String profession,
        int professionLevel,
        String costAItem,
        int costACount,
        String costBItem,
        int costBCount,
        String resultItem,
        int resultCount
) {

    public static final Codec<TradeEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("epochMillis").forGetter(TradeEntry::epochMillis),
            Codec.LONG.fieldOf("worldDay").forGetter(TradeEntry::worldDay),
            Codec.STRING.fieldOf("dimensionId").forGetter(TradeEntry::dimensionId),
            Codec.STRING.fieldOf("biomeId").forGetter(TradeEntry::biomeId),
            Codec.INT.fieldOf("x").forGetter(TradeEntry::x),
            Codec.INT.fieldOf("y").forGetter(TradeEntry::y),
            Codec.INT.fieldOf("z").forGetter(TradeEntry::z),
            Codec.STRING.optionalFieldOf("villagerUuid", "").forGetter(TradeEntry::villagerUuid),
            Codec.STRING.fieldOf("profession").forGetter(TradeEntry::profession),
            Codec.INT.fieldOf("professionLevel").forGetter(TradeEntry::professionLevel),
            Codec.STRING.fieldOf("costAItem").forGetter(TradeEntry::costAItem),
            Codec.INT.fieldOf("costACount").forGetter(TradeEntry::costACount),
            Codec.STRING.optionalFieldOf("costBItem", "").forGetter(TradeEntry::costBItem),
            Codec.INT.optionalFieldOf("costBCount", 0).forGetter(TradeEntry::costBCount),
            Codec.STRING.fieldOf("resultItem").forGetter(TradeEntry::resultItem),
            Codec.INT.fieldOf("resultCount").forGetter(TradeEntry::resultCount)
    ).apply(instance, TradeEntry::new));

    public boolean isEnchantedBook() {
        return "minecraft:enchanted_book".equals(resultItem);
    }

    public boolean isSingleCost() {
        return costBItem == null || costBItem.isEmpty() || costBCount == 0;
    }

    /** Compat shim - villager custom name field was dropped to fit Codec's 16-field cap. */
    public String villagerName() {
        return "";
    }
}
