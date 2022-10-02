package com.almostreliable.merequester;

import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.almostreliable.merequester.mixin.AEItemsInvokerMixin;
import com.almostreliable.merequester.requester.RequesterTerminal;
import com.almostreliable.merequester.utils.Utils;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final String TERMINAL_ID = "requester_terminal";

    private static final Tab TAB = new Tab();
    private static final ItemDefinition<PartItem<RequesterTerminal>> TERMINAL = createTerminal();

    public MERequester() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(MERequester::registryEvent);
    }

    private static void registryEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
            ForgeRegistries.ITEMS.register(TERMINAL.id(), TERMINAL.asItem());
        }
    }

    private static ItemDefinition<PartItem<RequesterTerminal>> createTerminal() {
        PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminal.class));
        return AEItemsInvokerMixin.merequester$partItem(
            "",
            Utils.getRL(TERMINAL_ID),
            props -> new PartItem<>(props, RequesterTerminal.class, RequesterTerminal::new),
            TAB
        );
    }

    private static final class Tab extends CreativeModeTab {

        private Tab() {
            super(BuildConfig.MOD_ID);
        }

        @Override
        public ItemStack makeIcon() {
            return TERMINAL.stack();
        }
    }
}
