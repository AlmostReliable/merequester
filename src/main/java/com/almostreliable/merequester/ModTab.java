package com.almostreliable.merequester;

import com.almostreliable.merequester.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class ModTab {

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Utils.getRL("tab")
    );
    private static final CreativeModeTab TAB = Platform.createTab();

    private ModTab() {}

    static void initContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TAB_KEY) {
            event.accept(Registration.REQUESTER);
            event.accept(Registration.TERMINAL);
        }
    }

    static void registerTab(RegisterEvent registerEvent) {
        registerEvent.register(Registries.CREATIVE_MODE_TAB, TAB_KEY.location(), () -> TAB);
    }
}
