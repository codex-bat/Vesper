package dev.codexbat.vesper.api.blockentity;

import dev.codexbat.vesper.api.util.scatter.StackableScatterState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public abstract class StackableBlockEntity extends BlockEntity implements StackableScatterState {
    private boolean scatterPaused;
    private boolean fragile;
    private int scatterCooldown;

    protected StackableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean vesper$isScatterPaused() {
        return scatterPaused;
    }

    @Override
    public void vesper$setScatterPaused(boolean paused) {
        this.scatterPaused = paused;
        this.markDirty();
    }

    @Override
    public boolean vesper$isFragile() {
        return fragile;
    }

    @Override
    public void vesper$setFragile(boolean fragile) {
        this.fragile = fragile;
        this.markDirty();
    }

    @Override
    public int vesper$getScatterCooldown() {
        return scatterCooldown;
    }

    @Override
    public void vesper$setScatterCooldown(int ticks) {
        this.scatterCooldown = Math.max(0, ticks);
        this.markDirty();
    }

    public static void tick(net.minecraft.world.World world,
                            BlockPos pos,
                            BlockState state,
                            StackableBlockEntity be) {
        if (world.isClient()) return;
        if (!(state.getBlock() instanceof dev.codexbat.vesper.api.block.stackable.StackableBlock stackable)) return;
        dev.codexbat.vesper.api.util.scatter.ScatterLogic.tick(world, pos, state, be, stackable);
    }
}