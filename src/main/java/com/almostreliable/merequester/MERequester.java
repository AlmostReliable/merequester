package com.almostreliable.merequester;

import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final String TERMINAL_ID = "requester_terminal";
    public static final String REQUESTER_ID = "requester";

    public static final ItemDefinition<PartItem<RequesterTerminalPart>> TERMINAL = Registration.setupTerminal();
    public static final BlockDefinition<RequesterBlock> REQUESTER = Registration.setupRequester();

    public MERequester() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(Registration::registryEvent);
    }
}
