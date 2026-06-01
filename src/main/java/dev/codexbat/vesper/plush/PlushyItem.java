package dev.codexbat.vesper.plush;

import dev.codexbat.vesper.plush.block.PlushyBlock;
import dev.codexbat.vesper.plush.block.entity.PlushyBlockEntity;
import dev.codexbat.vesper.plush.entity.PlushyEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/** Placed on a surface to spawn a PlushyEntity. Item model stays as the converted Blockbench JSON. */
public class PlushyItem extends Item {

    private final PlushyDefinition definition;

    public PlushyItem(PlushyDefinition definition, Settings settings) {
        super(settings);
        this.definition = definition;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos().offset(ctx.getSide());

        // Reject non-solid surfaces (tall grass, flowers, panes, etc.) — same check vanilla uses
        if (!world.getBlockState(ctx.getBlockPos())
                .isSideSolid(world, ctx.getBlockPos(), ctx.getSide(), SideShapeType.FULL)) {
            return ActionResult.PASS;
        }

        // Target spot must be empty — PASS keeps the arm from swinging
        if (!world.getBlockState(pos).isAir()) {
            return ActionResult.PASS;
        }

        // Spot is free: client confirms the swing, server does the work
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        // Face toward the player, same convention as a furnace
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockState state = PlushyRegistry.PLUSHY_BLOCK.getDefaultState()
                .with(PlushyBlock.FACING, facing);

        if (!world.setBlockState(pos, state, Block.NOTIFY_ALL)) {
            return ActionResult.FAIL;
        }

        if (world.getBlockEntity(pos) instanceof PlushyBlockEntity be) {
            be.setDefinitionId(definition.getId());
        }

        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_WOOL_PLACE,
                SoundCategory.BLOCKS,
                1.0f,
                0.8f + world.random.nextFloat() * 0.4f
        );

        PlayerEntity player = ctx.getPlayer();
        if (player != null && !player.isCreative()) {
            ctx.getStack().decrement(1);
        }

        return ActionResult.CONSUME;
    }

    public PlushyDefinition getDefinition() { return definition; }
}