package dev.codexbat.vesper.api.util.scatter.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public final class ScatterWorldgen {
    private ScatterWorldgen() {}

    public static BlockScatterBuilder block(BlockState state) {
        return new BlockScatterBuilder(state);
    }

    public static CollectionScatterBuilder collection(Block collectionBlock, BlockState scatteredBlock) {
        return new CollectionScatterBuilder(collectionBlock, scatteredBlock);
    }

    public static StructureScatterBuilder structure(Identifier structureId) {
        return new StructureScatterBuilder(structureId);
    }

    public static final class BlockScatterBuilder {
        private final BlockState state;
        private int radius = 4;
        private float density = 0.35f;
        private int attempts = 6;
        private Predicate<BlockState> canReplace = BlockState::isAir;
        private Predicate<BlockState> canPlaceOn = s -> !s.isAir();

        private BlockScatterBuilder(BlockState state) {
            this.state = state;
        }

        public BlockScatterBuilder radius(int radius) { this.radius = radius; return this; }
        public BlockScatterBuilder density(float density) { this.density = density; return this; }
        public BlockScatterBuilder attempts(int attempts) { this.attempts = attempts; return this; }
        public BlockScatterBuilder canReplace(Predicate<BlockState> canReplace) { this.canReplace = canReplace; return this; }
        public BlockScatterBuilder canPlaceOn(Predicate<BlockState> canPlaceOn) { this.canPlaceOn = canPlaceOn; return this; }

        public BlockScatterSpec build() {
            return new BlockScatterSpec(state, radius, density, attempts, canReplace, canPlaceOn);
        }
    }

    public static final class CollectionScatterBuilder {
        private final Block collectionBlock;
        private final BlockState scatteredBlock;
        private int radius = 4;
        private int maxCount = 8;
        private float density = 0.35f;
        private Predicate<BlockState> canReplace = BlockState::isAir;
        private Predicate<BlockState> canPlaceOn = s -> !s.isAir();

        private CollectionScatterBuilder(Block collectionBlock, BlockState scatteredBlock) {
            this.collectionBlock = collectionBlock;
            this.scatteredBlock = scatteredBlock;
        }

        public CollectionScatterBuilder radius(int radius) { this.radius = radius; return this; }
        public CollectionScatterBuilder maxCount(int maxCount) { this.maxCount = maxCount; return this; }
        public CollectionScatterBuilder density(float density) { this.density = density; return this; }
        public CollectionScatterBuilder canReplace(Predicate<BlockState> canReplace) { this.canReplace = canReplace; return this; }
        public CollectionScatterBuilder canPlaceOn(Predicate<BlockState> canPlaceOn) { this.canPlaceOn = canPlaceOn; return this; }

        public CollectionScatterSpec build() {
            return new CollectionScatterSpec(collectionBlock, scatteredBlock, radius, maxCount, density, canReplace, canPlaceOn);
        }
    }

    public static final class StructureScatterBuilder {
        private final Identifier structureId;
        private int radius = 16;
        private float density = 0.1f;
        private int attemptsPerChunk = 2;
        private int minY = -64;
        private int maxY = 320;

        private StructureScatterBuilder(Identifier structureId) {
            this.structureId = structureId;
        }

        public StructureScatterBuilder radius(int radius) { this.radius = radius; return this; }
        public StructureScatterBuilder density(float density) { this.density = density; return this; }
        public StructureScatterBuilder attemptsPerChunk(int attemptsPerChunk) { this.attemptsPerChunk = attemptsPerChunk; return this; }
        public StructureScatterBuilder minY(int minY) { this.minY = minY; return this; }
        public StructureScatterBuilder maxY(int maxY) { this.maxY = maxY; return this; }

        public StructureScatterSpec build() {
            return new StructureScatterSpec(structureId, radius, density, attemptsPerChunk, minY, maxY);
        }
    }
}