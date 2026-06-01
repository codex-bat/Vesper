package dev.codexbat.vesper.api.block.stackable;

import net.minecraft.block.BlockState;

public record StackDecayConfig(
        int fragileThreshold,
        int stableMin,
        int stableMax,
        float chancePerTick,
        int scatterRadius,
        int scatterAttempts,
        BlockState scatteredBlock
) {
    public StackDecayConfig {
        if (stableMin < 1) throw new IllegalArgumentException("stableMin must be >= 1");
        if (stableMax < stableMin) throw new IllegalArgumentException("stableMax must be >= stableMin");
        if (fragileThreshold < stableMax) throw new IllegalArgumentException("fragileThreshold must be >= stableMax");
        if (chancePerTick < 0.0f || chancePerTick > 1.0f) throw new IllegalArgumentException("chancePerTick must be 0..1");
        if (scatterRadius < 1) throw new IllegalArgumentException("scatterRadius must be >= 1");
        if (scatterAttempts < 1) throw new IllegalArgumentException("scatterAttempts must be >= 1");
    }

    public boolean isStable(int count) {
        return count >= stableMin && count <= stableMax;
    }

    public boolean isFragile(int count) {
        return count > fragileThreshold;
    }
}