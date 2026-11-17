package com.almostreliable.merequester.core;

import com.almostreliable.merequester.MERequester;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    private static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    public static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static final class CommonConfig {

        public final ModConfigSpec.IntValue requests;
        public final ModConfigSpec.DoubleValue idleEnergy;
        public final ModConfigSpec.BooleanValue requireChannel;
        public final ModConfigSpec.IntValue craftingRetryTicks;
        public final ModConfigSpec.IntValue maxConcurrentPlans;

        private CommonConfig(ModConfigSpec.Builder builder) {
            builder.push(MERequester.REQUESTER_ID);
            requests = builder.comment("The amount of requests a single ME Requester can hold.").defineInRange("requests", 5, 1, 64);
            idleEnergy = builder
                .comment("The amount of energy (in AE) the ME Requester drains from the ME network when idle.")
                .defineInRange("idle_energy", 5.0, 0.0, Double.MAX_VALUE);
            requireChannel = builder
                .comment("Whether the ME Requester requires an ME network channel to function.")
                .define("require_channel", true);
            craftingRetryTicks = builder
                .comment(
                    "How many ticks a request waits after a crafting plan fails before it is planned again. Applies when ingredients are missing or no crafting CPU can take the job. Lower values restock sooner and start more AE2 crafting calculations."
                )
                .defineInRange("crafting_retry_ticks", 100, 1, 12000);
            maxConcurrentPlans = builder
                .comment(
                    "How many ME Requester crafting plans may be calculated at the same time on one level. Plans run off the server thread. Higher values restock more requests in parallel and use more CPU."
                )
                .defineInRange("max_concurrent_plans", 4, 1, 64);
            builder.pop();
        }
    }
}
