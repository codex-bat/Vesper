package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import org.joml.Quaternionf;

/**
 * World-space glint particle that physically tracks a Minecraft entity.
 *
 * Instead of an absolute world position, this particle stores an entity
 * reference and a fixed offset from that entity's feet. Each tick,
 * this.x/y/z is updated to entity.getX/Y/Z() + offset. The standard
 * BillboardParticle lerp between lastX/Y/Z and x/y/z then interpolates
 * entity movement identically to how the entity model renderer does it,
 * so camera-relative position is stable regardless of camera movement.
 *
 * Without this, particles spawn at tick-time world positions while the
 * camera interpolates smoothly between ticks, causing the particle cloud
 * to drift relative to the item every render frame.
 */
@Environment(EnvType.CLIENT)
public final class WorldGlintParticle extends BillboardParticle {

    private static final float WORLD_PIXEL = 0.016f;

    private final Entity trackedEntity;
    /** Offset from entity feet to particle spawn, in world-axis space. Baked at spawn. */
    private final double offsetX, offsetY, offsetZ;

    private final ParticleConfig.Shape shape;
    private final boolean fadeOut;
    private final float quadHalfSize;
    private final float baseAlpha;

    WorldGlintParticle(ClientWorld world,
                       Entity trackedEntity,
                       double offsetX, double offsetY, double offsetZ,
                       Sprite sprite,
                       ParticleConfig config,
                       float alphaMultiplier) {
        super(world,
                trackedEntity.getX() + offsetX,
                trackedEntity.getY() + offsetY,
                trackedEntity.getZ() + offsetZ,
                sprite);

        this.trackedEntity = trackedEntity;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        this.shape   = config.getShape();
        this.fadeOut = config.isFadeOut();

        // Config lifetime is screen-space frames (60 fps). Convert to game ticks (20 tps).
        this.maxAge = Math.max(1, Math.round(config.getLifetime() * WorldGlintParticleManager.LIFETIME_SCALE));
        this.age    = 0;

        float rawSize = config.getSizeMin()
                + this.random.nextFloat() * (config.getSizeMax() - config.getSizeMin());
        this.quadHalfSize = rawSize * WORLD_PIXEL * 0.5f;
        this.scale = 1.0f;

        int argb = config.getColor();
        this.baseAlpha = ((argb >> 24) & 0xFF) / 255.0f * alphaMultiplier;
        this.red       = ((argb >> 16) & 0xFF) / 255.0f;
        this.green     = ((argb >>  8) & 0xFF) / 255.0f;
        this.blue      = ( argb        & 0xFF) / 255.0f;
        this.alpha     = this.baseAlpha;

        this.collidesWithWorld = false;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    /**
     * Save last position, then advance to entity's current tick position.
     *
     * lastX = entity.getX() at end of the PREVIOUS tick
     * this.x = entity.getX() at end of THIS tick
     *
     * lerp(tickProgress, lastX, x) in render() then equals
     * lerp(tickProgress, entity.lastX, entity.getX()) + offsetX,
     * which is exactly how the entity model interpolates its own position.
     * Camera.getCameraPos() uses the same interpolation, so they cancel:
     * the particle appears locked to the item regardless of camera motion.
     */
    @Override
    public void tick() {
        if (this.trackedEntity.isRemoved()) {
            this.markDead();
            return;
        }

        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.x = this.trackedEntity.getX() + this.offsetX;
        this.y = this.trackedEntity.getY() + this.offsetY;
        this.z = this.trackedEntity.getZ() + this.offsetZ;

        this.alpha = this.fadeOut
                ? this.baseAlpha * (1.0f - (float) this.age / this.maxAge)
                : this.baseAlpha;
    }

    @Override
    protected void render(BillboardParticleSubmittable submittable,
                          Camera camera, Quaternionf rotation, float tickProgress) {
        if (this.alpha <= 0.0f || this.trackedEntity.isRemoved()) return;

        float ix = (float)(lerp(tickProgress, this.lastX, this.x) - camera.getCameraPos().x);
        float iy = (float)(lerp(tickProgress, this.lastY, this.y) - camera.getCameraPos().y);
        float iz = (float)(lerp(tickProgress, this.lastZ, this.z) - camera.getCameraPos().z);

        switch (this.shape) {
            case SQUARE  -> drawQuad(submittable, rotation, ix, iy, iz, quadHalfSize);
            case DIAMOND -> drawDiamond(submittable, rotation, ix, iy, iz);
            case CROSS   -> drawCross(submittable, rotation, ix, iy, iz);
            case CIRCLE  -> drawCircle(submittable, rotation, ix, iy, iz);
        }
    }

    private void drawQuad(BillboardParticleSubmittable sub, Quaternionf rot,
                          float cx, float cy, float cz, float half) {
        sub.render(
                getRenderType(),
                cx, cy, cz,
                rot.x, rot.y, rot.z, rot.w,
                half * 2.0f,
                getMinU(), getMaxU(),
                getMinV(), getMaxV(),
                ColorHelper.fromFloats(this.alpha, this.red, this.green, this.blue),
                15728880 // full-bright
        );
    }

    private void drawDiamond(BillboardParticleSubmittable sub, Quaternionf rot, float cx, float cy, float cz) {
        float h = quadHalfSize, o = h * 1.2f;
        drawQuad(sub, rot, cx,     cy,     cz, h);
        drawQuad(sub, rot, cx + o, cy,     cz, h * 0.6f);
        drawQuad(sub, rot, cx - o, cy,     cz, h * 0.6f);
        drawQuad(sub, rot, cx,     cy + o, cz, h * 0.6f);
        drawQuad(sub, rot, cx,     cy - o, cz, h * 0.6f);
    }

    private void drawCross(BillboardParticleSubmittable sub, Quaternionf rot, float cx, float cy, float cz) {
        float o = quadHalfSize * 2.0f;
        drawQuad(sub, rot, cx,     cy,     cz, quadHalfSize);
        drawQuad(sub, rot, cx + o, cy,     cz, quadHalfSize);
        drawQuad(sub, rot, cx - o, cy,     cz, quadHalfSize);
        drawQuad(sub, rot, cx,     cy + o, cz, quadHalfSize);
        drawQuad(sub, rot, cx,     cy - o, cz, quadHalfSize);
    }

    private void drawCircle(BillboardParticleSubmittable sub, Quaternionf rot, float cx, float cy, float cz) {
        float o = quadHalfSize * 1.8f;
        drawQuad(sub, rot, cx,     cy,     cz, quadHalfSize);
        drawQuad(sub, rot, cx + o, cy,     cz, quadHalfSize * 0.7f);
        drawQuad(sub, rot, cx - o, cy,     cz, quadHalfSize * 0.7f);
        drawQuad(sub, rot, cx,     cy + o, cz, quadHalfSize * 0.7f);
        drawQuad(sub, rot, cx,     cy - o, cz, quadHalfSize * 0.7f);
    }

    private static double lerp(float delta, double prev, double curr) {
        return prev + delta * (curr - prev);
    }
}