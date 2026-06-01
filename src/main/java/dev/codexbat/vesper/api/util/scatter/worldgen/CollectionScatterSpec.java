package dev.codexbat.vesper.api.util.scatter.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

/**
 * For custom collection blocks like pebbles, shells, shards, etc.
 * This is the "multiple instances of a smaller block" type you described.
 */
public record CollectionScatterSpec(
        Block collectionBlock,
        BlockState scatteredBlock,
        int radius,
        int maxCount,
        float density,
        Predicate<BlockState> canReplace,
        Predicate<BlockState> canPlaceOn
) implements ScatterWorldgenSpec {
}