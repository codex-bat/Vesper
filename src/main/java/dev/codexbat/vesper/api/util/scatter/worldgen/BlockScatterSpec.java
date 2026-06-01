package dev.codexbat.vesper.api.util.scatter.worldgen;

import net.minecraft.block.BlockState;

import java.util.function.Predicate;

public record BlockScatterSpec(
        BlockState state,
        int radius,
        float density,
        int attempts,
        Predicate<BlockState> canReplace,
        Predicate<BlockState> canPlaceOn
) implements ScatterWorldgenSpec {
}