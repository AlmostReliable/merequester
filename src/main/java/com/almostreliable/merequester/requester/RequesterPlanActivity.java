package com.almostreliable.merequester.requester;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks requester crafting plans that are currently running on AE2's calculator thread.
 * The server tick uses this to avoid reading the live pattern map on the same tick.
 */
public final class RequesterPlanActivity {

    private static final AtomicInteger AWAKE = new AtomicInteger();

    private RequesterPlanActivity() {}

    public static void enter() {
        AWAKE.incrementAndGet();
    }

    public static void exit() {
        AWAKE.updateAndGet(value -> Math.max(0, value - 1));
    }

    public static boolean isAwake() {
        return AWAKE.get() > 0;
    }
}
