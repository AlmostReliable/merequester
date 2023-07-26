package com.almostreliable.merequester;

import com.almostreliable.merequester.platform.Platform;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModTab {

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Utils.getRL("tab")
    );
    private static final CreativeModeTab TAB = Platform.createTab();

    private ModTab() {}

    @SuppressWarnings("UnstableApiUsage")
    static void initContents() {
        ItemGroupEvents.modifyEntriesEvent(TAB_KEY)
            .register(entries -> entries.addAfter(ItemStack.EMPTY, Registration.REQUESTER, Registration.TERMINAL));
    }

    static void registerTab() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, TAB);
    }
}
