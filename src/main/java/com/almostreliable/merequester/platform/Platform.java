package com.almostreliable.merequester.platform;

import com.almostreliable.merequester.BuildConfig;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;
import com.almostreliable.merequester.network.DragAndDropPacket;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.network.RequestUpdatePacket;
import com.almostreliable.merequester.network.RequesterSyncPacket;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public final class Platform {

    private Platform() {}

    public static void initConfig() {
        var modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    public static int getRequestLimit() {
        return Config.COMMON.requests.get();
    }

    public static double getIdleEnergy() {
        return Config.COMMON.idleEnergy.get();
    }

    public static boolean requireChannel() {
        return Config.COMMON.requireChannel.get();
    }

    public static CreativeModeTab createTab() {
        return new CreativeModeTab(BuildConfig.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                // noinspection deprecation
                return Registry.ITEM.get(Utils.getRL(MERequester.TERMINAL_ID)).getDefaultInstance();
            }
        };
    }

    public static void sendRequestUpdate(long requesterId, int requestIndex, boolean state) {
        PacketHandler.CHANNEL.sendToServer(new RequestUpdatePacket(requesterId, requestIndex, state));
    }

    public static void sendRequestUpdate(long requesterId, int requestIndex, long amount, long batch) {
        PacketHandler.CHANNEL.sendToServer(new RequestUpdatePacket(requesterId, requestIndex, amount, batch));
    }

    public static void sendDragAndDrop(long requesterId, int requestIndex, ItemStack item) {
        PacketHandler.CHANNEL.sendToServer(new DragAndDropPacket(requesterId, requestIndex, item));
    }

    public static void sendClearData(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                RequesterSyncPacket.clearData()
            );
        }
    }

    public static void sendInventoryData(Player player, long requesterId, CompoundTag data) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                RequesterSyncPacket.inventory(requesterId, data)
            );
        }
    }

    public static List<Widget> getRenderables(Screen screen) {
        return screen.renderables;
    }
}
