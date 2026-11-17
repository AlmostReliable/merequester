package com.almostreliable.merequester.requester.status;

import com.almostreliable.merequester.core.Config;
import com.almostreliable.merequester.requester.CraftingPlanBudget;
import com.almostreliable.merequester.requester.RequesterBlockEntity;

import net.minecraft.world.level.Level;

import appeng.api.networking.ticking.TickRateModulation;

import org.jetbrains.annotations.Nullable;

public abstract class BlockingState implements StatusState {

    private long retryAtGameTime = Long.MIN_VALUE;
    @Nullable
    private PlanState simulatedPlanState;

    BlockingState() {}

    static BlockingState coolDown(RequesterBlockEntity host, BlockingState state) {
        var level = host.getLevel();
        if (level != null) state.arm(level);
        return state;
    }

    void arm(Level level) {
        retryAtGameTime = level.getGameTime() + Config.COMMON.craftingRetryTicks.get();
    }

    @Override
    public void cancel() {
        if (simulatedPlanState != null) {
            simulatedPlanState.cancel();
            simulatedPlanState = null;
        }
    }

    @Override
    public StatusState handle(RequesterBlockEntity host, int slot) {
        if (simulatedPlanState != null) {
            var planSim = simulatedPlanState.handle(host, slot);
            if (planSim == simulatedPlanState) return this;
            simulatedPlanState = null;
            return planSim;
        }

        var idleSim = IDLE.handle(host, slot);
        if (idleSim == IDLE || idleSim == EXPORT) {
            // Enough of the item is stored, or a finished craft is waiting to be exported.
            return IDLE;
        }

        var level = host.getLevel();
        if (level != null && level.getGameTime() < retryAtGameTime) return this;
        if (level == null || !CraftingPlanBudget.hasRoom(level)) return this;

        var requestSim = REQUEST.handle(host, slot);
        if (requestSim instanceof PlanState plan) {
            simulatedPlanState = plan;
            return this;
        }
        // The request state stays put when every calculation slot is taken.
        if (requestSim == REQUEST) return this;
        return requestSim;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        if (simulatedPlanState != null) return simulatedPlanState.getTickRateModulation();
        return TickRateModulation.IDLE;
    }
}
