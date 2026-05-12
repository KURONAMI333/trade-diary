package com.kuronami.tradediary;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** Static constants shared across Trade Diary classes (MODID, LOGGER). */
public final class TradeDiary {
    public static final String MODID = "tradediary";
    public static final Logger LOGGER = LogUtils.getLogger();

    private TradeDiary() {}
}
