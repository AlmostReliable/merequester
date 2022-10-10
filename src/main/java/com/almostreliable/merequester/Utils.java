package com.almostreliable.merequester;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");

    private Utils() {}

    public static ResourceLocation getRL(String path) {
        return new ResourceLocation(BuildConfig.MOD_ID, path);
    }

    public static String f(String input, Object... args) {
        for (var arg : args) {
            input = PLACEHOLDER.matcher(input).replaceFirst(arg.toString());
        }
        for (var i = 0; i < args.length; i++) {
            input = input.replace("{" + i + "}", args[i].toString());
        }
        return input;
    }

    public static String translateAsString(String type, String key) {
        return Component.translatable(getTranslationKey(type, key)).getString();
    }

    public static <T> T cast(Object o, Class<T> clazz) {
        return clazz.cast(o);
    }

    private static String getTranslationKey(String type, String key) {
        return f("{}.{}.{}", type.toLowerCase(), BuildConfig.MOD_ID, key);
    }
}
