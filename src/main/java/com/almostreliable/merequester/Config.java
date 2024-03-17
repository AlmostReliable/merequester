package com.almostreliable.merequester;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    public static final class CommonConfig {

        public final ModConfigSpec.IntValue requests;
        public final ModConfigSpec.DoubleValue idleEnergy;
        public final ModConfigSpec.BooleanValue requireChannel;

        private CommonConfig(ModConfigSpec.Builder builder) {
            builder.push(MERequester.REQUESTER_ID);
            requests = builder.comment("The amount of requests a single ME Requester can hold.").defineInRange("requests", 5, 1, 64);
            idleEnergy = builder
                .comment("The amount of energy (in AE) the ME Requester drains from the ME network when idle.")
                .defineInRange("idle_energy", 5.0, 0.0, Double.MAX_VALUE);
            requireChannel = builder
                .comment("Whether the ME Requester requires an ME network channel to function.")
                .define("require_channel", true);
            builder.pop();
        }
    }
}
