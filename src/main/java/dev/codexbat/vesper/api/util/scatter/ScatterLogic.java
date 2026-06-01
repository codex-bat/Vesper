package dev.codexbat.vesper.api.util.scatter;

import dev.codexbat.vesper.api.block.stackable.StackDecayConfig;
import dev.codexbat.vesper.api.block.stackable.StackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public final class ScatterLogic {
    private ScatterLogic() {}

    public static void pause(BlockEntity be) {
        if (be instanceof StackableScatterState state) {
            state.vesper$setScatterPaused(true);
            be.markDirty();
        }
    }

    public static void resume(BlockEntity be) {
        if (be instanceof StackableScatterState state) {
            state.vesper$setScatterPaused(false);
            be.markDirty();
        }
    }

    public static boolean tick(World world,
                               BlockPos pos,
                               BlockState state,
                               StackableScatterState be,
                               StackableBlock stackable) {
        if (world == null || world.isClient()) return false;
        if (be.vesper$isScatterPaused()) return false;

        if (be.vesper$getScatterCooldown() > 0) {
            be.vesper$setScatterCooldown(be.vesper$getScatterCooldown() - 1);
            return false;
        }

        StackDecayConfig config = stackable.vesper$getDecayConfig(state);
        int currentCount = stackable.vesper$getStackCount(state);

        if (config.isStable(currentCount)) {
            be.vesper$setFragile(false);
            return false;
        }

        if (!be.vesper$isFragile()) {
            if (!config.isFragile(currentCount)) return false;
            be.vesper$setFragile(true);
        }

        if (ThreadLocalRandom.current().nextFloat() > config.chancePerTick()) {
            return false;
        }

        int nextCount = Math.max(config.stableMin(), currentCount - 1);
        BlockState nextState = stackable.vesper$withStackCount(state, nextCount);
        world.setBlockState(pos, nextState, 3);

        scatterOne(world, pos, config.scatterRadius(), config.scatterAttempts(), config.scatteredBlock());

        if (config.isStable(nextCount)) {
            be.vesper$setFragile(false);
        }

        be.vesper$setScatterCooldown(1);
        be.vesper$setScatterPaused(false);
        be.vesper$setScatterPaused(be.vesper$isScatterPaused());
        if (be instanceof BlockEntity blockEntity) {
            blockEntity.markDirty();
        }
        return true;
    }

    private static boolean scatterOne(World world,
                                      BlockPos origin,
                                      int radius,
                                      int attempts,
                                      BlockState scatteredBlock) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(-radius, radius + 1);
            int dy = random.nextInt(-1, 2);
            int dz = random.nextInt(-radius, radius + 1);

            BlockPos target = origin.add(dx, dy, dz);
            if (world.getBlockState(target).isAir()) {
                world.setBlockState(target, scatteredBlock, 3);
                return true;
            }
        }

        for (Direction direction : Direction.values()) {
            BlockPos target = origin.offset(direction);
            if (world.getBlockState(target).isAir()) {
                world.setBlockState(target, scatteredBlock, 3);
                return true;
            }
        }

        return false;
    }
}