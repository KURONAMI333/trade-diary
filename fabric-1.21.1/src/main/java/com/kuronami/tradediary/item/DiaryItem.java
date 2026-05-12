package com.kuronami.tradediary.item;

import com.kuronami.tradediary.client.TradeClientCache;
import com.kuronami.tradediary.network.RequestDiaryPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Journal item (Fabric 1.21.1). Right-click → open the diary viewer.
 *
 * <p>Same one-shot flow as the keybinding: prime the client cache, send a
 * request payload, the server responds, the screen opens. Server side is
 * a no-op — every effect happens client-side.
 */
public final class DiaryItem extends Item {

    public DiaryItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            openClient();
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Environment(EnvType.CLIENT)
    private static void openClient() {
        TradeClientCache.requestOpen();
        ClientPlayNetworking.send(RequestDiaryPayload.INSTANCE);
    }
}
