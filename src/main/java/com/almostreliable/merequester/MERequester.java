package com.almostreliable.merequester;

import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.platform.Platform;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    public MERequester() {
        onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
            var client = new MERequesterClient();
            return client::onInitialize;
        });
    }

    public void onInitialize() {
        Platform.initConfig();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MERequester::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}
