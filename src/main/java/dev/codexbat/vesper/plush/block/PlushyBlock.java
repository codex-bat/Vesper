package dev.codexbat.vesper.plush.block;

import com.mojang.serialization.MapCodec;
import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.plush.block.entity.PlushyBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PlushyBlock extends BlockWithEntity {

    public static final MapCodec<PlushyBlock> CODEC = createCodec(PlushyBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NS =
            Block.createCuboidShape(4.5, 0, 5.5, 11.5, 12.5, 10.5);
    private static final VoxelShape SHAPE_EW =
            Block.createCuboidShape(5.5, 0, 4.5, 10.5, 12.5, 11.5);

    private static VoxelShape shape(BlockState state) {
        Direction f = state.get(FACING);
        return (f == Direction.EAST || f == Direction.WEST) ? SHAPE_EW : SHAPE_NS;
    }

    public PlushyBlock(Settings settings) {
        super(settings);
        // Explicit default so the block always has a well-defined facing on first placement.
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    // Placement

    /** Prevents placement on non-solid surfaces (glass panes, fences, slabs, ...). */
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        return world.getBlockState(below).isSideSolid(world, below, Direction.UP, SideShapeType.FULL);
    }

    /**
     * Breaks and drops when the supporting block is removed.
     * A loot table at data/modid/loot_tables/blocks/block.json is required
     * for the item to actually be dropped on the ground.
     */
    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        return canPlaceAt(state, world, pos)
                ? super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
                : Blocks.AIR.getDefaultState();
    }

    // Block entity

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlushyBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type
    ) {
        return validateTicker(
                type,
                PlushyRegistry.PLUSHY_BLOCK_ENTITY,
                (w, p, s, be) -> be.tick()
        );
    }

    // Interaction

    @Override
    protected ActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (world.getBlockEntity(pos) instanceof PlushyBlockEntity plushy) {
            // squish() returns false while an entity is standing on top - skip sound then.
            if (plushy.squish()) {
                world.playSound(
                        null, pos,
                        SoundEvents.BLOCK_WOOL_HIT, SoundCategory.BLOCKS,
                        0.8f, 0.9f + world.random.nextFloat() * 0.2f
                );
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
        if (world.isClient()) return;
        if (world.getBlockEntity(pos) instanceof PlushyBlockEntity plushy) {
            plushy.squish(); // no-op while held; return value intentionally ignored here
        }
    }

    // Creative pick

    /** Middle-click in creative returns this block's item. */
    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.asItem());
    }

    // Rendering

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    // Shapes

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return shape(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return shape(state);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return shape(state);
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {
        return shape(state);
    }
}