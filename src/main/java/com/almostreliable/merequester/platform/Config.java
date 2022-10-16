package com.almostreliable.merequester.platform;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "NonConstantFieldWithUpperCaseName", "StaticNonFinalField", "WeakerAccess"})
public class Config extends MidnightConfig {
    @Entry(min = 1, max = 64) public static int REQUESTS = 5;
    @Entry(min = 0.0) public static double IDLE_ENERGY = 5.0;
    @Entry public static boolean REQUIRE_CHANNEL = true;
}
