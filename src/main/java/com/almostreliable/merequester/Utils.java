package com.almostreliable.merequester;

import net.minecraft.resources.ResourceLocation;

public final class Utils {

    private Utils() {}

    public static <T> T cast(Object o, Class<T> clazz) {
        return clazz.cast(o);
    }

    public static ResourceLocation getRL(String path) {
        return new ResourceLocation(BuildConfig.MOD_ID, path);
    }
}
