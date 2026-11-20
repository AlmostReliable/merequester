package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.requester.RequesterBlockEntity;

import appeng.api.networking.ticking.TickRateModulation;

import org.jetbrains.annotations.Nullable;

public abstract class BlockingState implements StatusState {

    private static final int MIN_RETRY_DELAY = 20;
    private static final int MAX_RETRY_DELAY = 600;

    private int retryDelay = MIN_RETRY_DELAY;
    private long retryTick = -1;
    @Nullable
    private PlanState retryPlan;

    BlockingState() {}

    @Override
    public StatusState handle(RequesterBlockEntity host, int slot) {
        var level = host.getLevel();
        if (level == null) return this;

        if (retryPlan != null) {
            // do not re-plan if previous plan is still calculating
            if (retryPlan.isNotDone()) return this;

            var result = retryPlan.handle(host, slot);
            retryPlan = null;

            if (result instanceof BlockingState blockingState) {
                // plan failed again, increase delay
                blockingState.retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY);
            }

            return result;
        }

        // enough is stored or the request was disabled, pending export is handled by idle state
        if (IDLE.handle(host, slot) != REQUEST) return IDLE;

        var gameTime = level.getGameTime();
        if (retryTick < 0) {
            // spread retries of multiple requests that failed at the same time
            retryTick = gameTime + retryDelay + level.getRandom().nextInt(retryDelay / 4 + 1);
        }

        if (gameTime < retryTick || !canRetry(host)) return this;

        var requestResult = REQUEST.handle(host, slot);
        if (!(requestResult instanceof PlanState planState)) return requestResult;

        retryPlan = planState;
        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }

    boolean canRetry(RequesterBlockEntity host) {
        return true;
    }
}
