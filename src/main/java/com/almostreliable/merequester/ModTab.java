package com.almostreliable.merequester;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModTab {

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Utils.getRL("tab"));
    private static final CreativeModeTab TAB = CreativeModeTab.builder()
        .title(Utils.translate("itemGroup", "tab"))
        .icon(() -> Registration.REQUESTER.stack())
        .noScrollBar()
        .build();

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
