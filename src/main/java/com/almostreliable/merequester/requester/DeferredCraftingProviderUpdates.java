package com.almostreliable.merequester.requester;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import appeng.me.service.CraftingService;

/**
 * Pattern-provider updates mutate the same maps a requester plan reads on the calculator thread.
 * Those updates are queued while a plan is awake and applied on the server thread once every plan is paused.
 */
public final class DeferredCraftingProviderUpdates {

    private static final Map<CraftingService, List<Runnable>> QUEUES = new IdentityHashMap<>();
    private static boolean flushing;

    private DeferredCraftingProviderUpdates() {}

    public static void defer(CraftingService service, Runnable update) {
        QUEUES.computeIfAbsent(service, key -> new ArrayList<>()).add(update);
    }

    public static void flush() {
        if (flushing || RequesterPlanActivity.isAwake() || QUEUES.isEmpty()) return;

        flushing = true;
        try {
            while (!QUEUES.isEmpty() && !RequesterPlanActivity.isAwake()) {
                var entry = QUEUES.entrySet().iterator().next();
                var updates = entry.getValue();
                QUEUES.remove(entry.getKey());
                for (var update : updates) {
                    update.run();
                }
            }
        } finally {
            flushing = false;
        }
    }
}
