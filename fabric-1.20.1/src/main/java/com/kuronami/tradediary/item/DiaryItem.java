package com.kuronami.tradediary.item;

import com.kuronami.tradediary.client.TradeClientCache;
import com.kuronami.tradediary.network.TradeDiaryNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Journal item (Fabric 1.20.1). Right-click -> open the diary viewer.
 * Uses 1.20.1's raw FriendlyByteBuf channel API.
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
        ClientPlayNetworking.send(TradeDiaryNetwork.REQUEST_ID, PacketByteBufs.empty());
    }
}
