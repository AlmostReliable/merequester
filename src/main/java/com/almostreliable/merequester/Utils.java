package com.almostreliable.merequester;

import com.almostreliable.merequester.BuildConfig;
import net.minecraft.resources.ResourceLocation;

public final class Utils {

    private Utils() {}

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static ResourceLocation getRL(String path) {
        return new ResourceLocation(BuildConfig.MOD_ID, path);
    }

    public static String prefix(String path) {
        return BuildConfig.MOD_ID + "." + path;
    }
}
