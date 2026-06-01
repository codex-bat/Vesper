package dev.codexbat.vesper.client.plush.render;

import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.plush.block.PlushyBlock;
import dev.codexbat.vesper.plush.block.entity.PlushyBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlushyBlockEntityRenderer
        implements BlockEntityRenderer<PlushyBlockEntity, PlushyBlockEntityRenderer.PlushyRenderState> {

    private final BlockStateModel model;

    public PlushyBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        BlockState state = PlushyRegistry.PLUSHY_BLOCK.getDefaultState();
        this.model = ctx.renderManager().getModels().getModel(state);
    }

    public static final class PlushyRenderState extends BlockEntityRenderState {
        public float squish;
        public Direction facing = Direction.NORTH;
    }

    @Override
    public PlushyRenderState createRenderState() {
        return new PlushyRenderState();
    }

    @Override
    public void updateRenderState(
            PlushyBlockEntity blockEntity,
            PlushyRenderState state,
            float tickProgress,
            Vec3d cameraPos,
            ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.squish = blockEntity.getSquish(tickProgress);
        state.facing = blockEntity.getCachedState().get(PlushyBlock.FACING);
    }

    @Override
    public void render(
            PlushyRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        float baseScale = 0.943f;
        float squishScaleY = 1.0f - (state.squish * 0.08f);

        float yaw = switch (state.facing) {
            case WEST -> 90.0f;
            case SOUTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };

        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.scale(baseScale, baseScale * squishScaleY, baseScale);
        matrices.translate(-0.5, 0.0, -0.5);

        queue.submitBlockStateModel(
                matrices,
                RenderLayers.cutout(),
                this.model,
                1.0f, 1.0f, 1.0f,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }
}