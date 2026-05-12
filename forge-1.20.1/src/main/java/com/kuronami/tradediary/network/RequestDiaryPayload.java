package com.kuronami.tradediary.network;

import net.minecraft.network.FriendlyByteBuf;

public record RequestDiaryPayload() {
    public static final RequestDiaryPayload INSTANCE = new RequestDiaryPayload();
    public void encode(FriendlyByteBuf buf) {}
    public static RequestDiaryPayload decode(FriendlyByteBuf buf) { return INSTANCE; }
}
