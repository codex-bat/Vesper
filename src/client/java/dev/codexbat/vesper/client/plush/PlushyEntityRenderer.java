package dev.codexbat.vesper.client.plush;

import dev.codexbat.vesper.plush.PlushyDefinition;
import dev.codexbat.vesper.plush.entity.PlushyEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class PlushyEntityRenderer
        extends EntityRenderer<PlushyEntity, PlushyEntityRenderer.State> {

    /**
     * Final droop angle in degrees. The arm animation eases into this value.
     * Tune to 22.5f for a softer resting pose, or leave at 45f for a more
     * relaxed "arms hanging" look.
     */
    private static final float ARM_DROOP_DEG = 45f;
    private static final float BOB_AMPLITUDE = 0.04f;

    private static final Identifier MISSING =
            Identifier.of("minecraft", "textures/misc/missing.png");

    public PlushyEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.3f;
    }

    // ── Render state ──────────────────────────────────────────────────────────

    public static final class State extends EntityRenderState {
        public float arm          = 0f;
        public float bob          = 0f;
        public float bodyYaw      = 0f;
        public float squishNorm   = 0f; // ← NEW: 0 = resting, 1 = full squish
        public Identifier texture = null;
        public PlushyModelData model = null;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void updateRenderState(PlushyEntity entity, State state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);

        state.arm        = MathHelper.lerp(tickProgress, entity.prevArmProgress, entity.armProgress);
        state.bob        = MathHelper.lerp(tickProgress, entity.prevBobPhase,    entity.bobPhase);
        state.bodyYaw    = entity.lerpYaw(tickProgress);
        state.squishNorm = entity.getSquishNorm(); // ← NEW

        PlushyDefinition def = entity.getDefinition();
        if (def != null) {
            state.texture = def.getEntityTexture();
            state.model   = PlushyModelRegistry.get(def.getId());
        } else {
            state.texture = MISSING;
            state.model   = null;
        }
    }

    // ── Main render ───────────────────────────────────────────────────────────

    @Override
    public void render(State state, MatrixStack ms,
                       OrderedRenderCommandQueue queue, CameraRenderState camera) {

        if (state.model != null && state.texture != null) {
            RenderLayer layer = RenderLayers.entityCutoutNoCull(state.texture);
            VertexConsumerProvider.Immediate immediate =
                    MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer vc = immediate.getBuffer(layer);
            ms.push();

            // 1. Idle bob (muted until arms are down)
            ms.translate(0.0, (float) Math.sin(state.bob) * BOB_AMPLITUDE * state.arm, 0.0);

            // 2. Squish scale — ← NEW
            // Quadratic ease-out: snaps in hard at the moment of the click,
            // then releases quickly. Bottom of the model stays grounded because
            // the scale origin is the entity's foot position (Y = 0).
            if (state.squishNorm > 0f) {
                float t = state.squishNorm * state.squishNorm; // quadratic ease-out
                ms.scale(1f + 0.12f * t,   // +12 % wider
                        1f - 0.18f * t,   // −18 % shorter
                        1f + 0.12f * t);
                // Tune multipliers freely. At squishNorm = 1 (peak):
                //   scaleXZ = 1.12, scaleY = 0.82 — a clear but not violent squish.
            }

            // 3. Face the right direction (models export south-facing by default)
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - state.bodyYaw));

            // 4. Static bones
            for (PlushyBone bone : state.model.staticBones()) {
                ms.push();
                for (PlushyCuboid cuboid : bone.cuboids()) {
                    PlushyCuboidRenderer.render(ms, vc, cuboid, state.light, OverlayTexture.DEFAULT_UV);
                }
                ms.pop();
            }

            // 5. Arm bones
            renderArmBone(ms, vc, state.model.leftArmBone(),  state.arm, true,  state.light);
            renderArmBone(ms, vc, state.model.rightArmBone(), state.arm, false, state.light);

            ms.pop();
        }

        super.render(state, ms, queue, camera); // shadow + nametag
    }

    // ── Per-arm animation ─────────────────────────────────────────────────────

    private void renderArmBone(MatrixStack ms, VertexConsumer vc,
                               PlushyBone bone, float arm,
                               boolean isLeft, int light) {
        ms.push();
        float px = bone.animPivX() / 16f;
        float py = bone.animPivY() / 16f;
        float pz = bone.animPivZ() / 16f;
        float angle = ARM_DROOP_DEG * arm;

        ms.translate(px, py, pz);
        ms.multiply(isLeft
                ? RotationAxis.POSITIVE_Z.rotationDegrees(angle)
                : RotationAxis.NEGATIVE_Z.rotationDegrees(angle));
        ms.translate(-px, -py, -pz);

        for (PlushyCuboid cuboid : bone.cuboids()) {
            PlushyCuboidRenderer.render(ms, vc, cuboid, light, OverlayTexture.DEFAULT_UV);
        }
        ms.pop();
    }
}