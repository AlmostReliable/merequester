package com.almostreliable.merequester;

import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    /*
        TODO:
        - add serialization for requesters
        - fix requester not detecting changes to the network
        - add drops
        - add recipes
        - give requester a proper model and texture
        - add screen to the requester block
        - fluid support
     */

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    public static final ItemDefinition<PartItem<RequesterTerminalPart>> TERMINAL = Registration.setupTerminal();
    public static final BlockDefinition<RequesterBlock> REQUESTER = Registration.setupRequester();

    public MERequester() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(MERequester::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}
