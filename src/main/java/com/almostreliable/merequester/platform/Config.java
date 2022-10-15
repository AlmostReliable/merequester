package com.almostreliable.merequester.platform;

import com.almostreliable.merequester.MERequester;
import net.minecraftforge.common.ForgeConfigSpec;

final class Config {

    static final ForgeConfigSpec COMMON_SPEC;
    static final CommonConfig COMMON;

    static {
        var commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    static final class CommonConfig {

        final ForgeConfigSpec.IntValue requests;
        final ForgeConfigSpec.DoubleValue idleEnergy;
        final ForgeConfigSpec.BooleanValue requireChannel;

        private CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push(MERequester.REQUESTER_ID);
            requests = builder.comment("The amount of requests a single ME Requester can hold.")
                .defineInRange("requests", 5, 1, 64);
            idleEnergy = builder.comment(
                    "The amount of energy (in AE) the ME Requester drains from the ME network when idle.")
                .defineInRange("idle_energy", 5.0, 0.0, Double.MAX_VALUE);
            requireChannel = builder.comment("Whether the ME Requester requires an ME network channel to function.")
                .define("require_channel", true);
            builder.pop();
        }
    }
}
