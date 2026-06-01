package dev.codexbat.vesper.api.block.stackable;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public abstract class StackableBlock extends BlockWithEntity implements StackCountAccess {
    public static final IntProperty STACK_COUNT = IntProperty.of("stack_count", 1, 64);

    private final int minStackCount;
    private final int maxStackCount;
    private final int defaultStackCount;
    private final StackDecayConfig decayConfig;

    protected StackableBlock(Settings settings,
                             int minStackCount,
                             int maxStackCount,
                             int defaultStackCount,
                             StackDecayConfig decayConfig) {
        super(settings);
        this.minStackCount = minStackCount;
        this.maxStackCount = maxStackCount;
        this.defaultStackCount = Math.max(minStackCount, Math.min(maxStackCount, defaultStackCount));
        this.decayConfig = decayConfig;
        this.setDefaultState(this.stateManager.getDefaultState().with(STACK_COUNT, this.defaultStackCount));
    }

    @Override
    public IntProperty vesper$getStackCountProperty() {
        return STACK_COUNT;
    }

    @Override
    public int vesper$getMinStackCount() {
        return minStackCount;
    }

    @Override
    public int vesper$getMaxStackCount() {
        return maxStackCount;
    }

    public StackDecayConfig vesper$getDecayConfig(BlockState state) {
        return decayConfig;
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(STACK_COUNT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(STACK_COUNT, defaultStackCount);
    }
}