package com.almostreliable.merequester;

import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@Mod(BuildConfig.MOD_ID)
public class MERequester {

    public MERequester() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
                // ForgeRegistries.ITEMS.register(Utils.getRL("requester_terminal"), Utils.cast(MERequester.REQUESTER_TERMINAL.get()));
            }
        });
    }

    private static final class Tab extends CreativeModeTab {

        private Tab() {
            super(BuildConfig.MOD_ID);
        }

        @Override
        public ItemStack makeIcon() {
            return Items.ACACIA_BOAT.getDefaultInstance();
        }
    }
}
