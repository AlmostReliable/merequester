package com.almostreliable.merequester.requester;

import com.almostreliable.merequester.core.Config;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

/**
 * Caps how many requester crafting plans are calculating on one level.
 * Finished and cancelled plans are dropped the next time the cap is checked,
 * so a plan that outlives its block entity cannot hold a slot forever.
 */
public final class CraftingPlanBudget {

    private static final Object LOCK = new Object();
    private static final Map<Level, List<Future<?>>> IN_FLIGHT = new WeakHashMap<>();

    private CraftingPlanBudget() {}

    public static boolean hasRoom(Level level) {
        synchronized (LOCK) {
            return inFlight(level) < Config.COMMON.maxConcurrentPlans.get();
        }
    }

    public static void track(Level level, Future<?> plan) {
        synchronized (LOCK) {
            var plans = IN_FLIGHT.computeIfAbsent(level, ignored -> new ArrayList<>());
            plans.removeIf(Future::isDone);
            plans.add(plan);
        }
    }

    private static int inFlight(Level level) {
        var plans = IN_FLIGHT.get(level);
        if (plans == null) return 0;
        plans.removeIf(Future::isDone);
        if (plans.isEmpty()) {
            IN_FLIGHT.remove(level);
            return 0;
        }
        return plans.size();
    }
}
