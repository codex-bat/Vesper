package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;

/**
 * Represents a single live screen-space particle spawned by
 * {@link SparkleGlint}.
 *
 * <p>Behaviourally identical to {@link GlintAmbientParticle} but kept as a
 * separate type so the two effects can diverge later (e.g. adding wobble,
 * colour-shift, or glow passes to sparkles without touching ambient).
 * Owned and updated exclusively by {@link SparkleParticleManager}.
 */
@Environment(EnvType.CLIENT)
public final class GlintSparkleParticle {

    private static final Random RANDOM = new Random();

    public double x;
    public double y;
    public double velX;
    public double velY;

    public int lifetime;
    public final int maxLifetime;

    public final int baseColor;
    public final float size;
    public final ParticleConfig.Shape shape;
    public final boolean fadeOut;
    private final float gravity;

    /**
     * Constructs a particle near the given screen origin using the provided config.
     * Position, velocity, and size are randomised within the config's ranges.
     *
     * @param originX Screen X of the spawn origin (slot center or cursor position).
     * @param originY Screen Y of the spawn origin.
     * @param config  The {@link ParticleConfig} driving this particle's behaviour.
     */
    public GlintSparkleParticle(double originX, double originY, ParticleConfig config) {
        this(originX, originY, config, 1.0f);
    }

    public GlintSparkleParticle(double originX, double originY, ParticleConfig config,
                                float alphaMultiplier) {
        this.maxLifetime = config.getLifetime();
        this.lifetime    = this.maxLifetime;
        this.shape       = config.getShape();
        this.fadeOut     = config.isFadeOut();
        this.gravity     = config.getGravity();
        this.size        = config.getSizeMin() + RANDOM.nextFloat() * (config.getSizeMax() - config.getSizeMin());

        // Bake the multiplier in so fadeOut ratios stay correct
        int raw   = config.getColor();
        int alpha = (int)(((raw >> 24) & 0xFF) * alphaMultiplier);
        this.baseColor = (alpha << 24) | (raw & 0x00FFFFFF);

        float spread = config.getSpread();
        this.x = originX + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;
        this.y = originY + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;

        float speed = config.getSpeed();
        this.velX = (RANDOM.nextFloat() - 0.5f)  * speed;
        this.velY = (RANDOM.nextFloat() - 0.75f) * speed;
    }

    /** Advances this particle by one frame. */
    public void update() {
        x    += velX;
        y    += velY;
        velY += gravity;
        velX *= 0.92f;
        velY *= 0.95f;
        lifetime--;
    }

    /** @return {@code true} when this particle has expired and should be removed. */
    public boolean isDead() { return lifetime <= 0; }

    /** Returns the current ARGB colour, with alpha faded if {@code fadeOut} is enabled. */
    public int getCurrentColor() {
        if (!fadeOut) return baseColor;
        float ratio   = (float) lifetime / maxLifetime;
        int baseAlpha = (baseColor >> 24) & 0xFF;
        int faded     = (int)(baseAlpha * ratio);
        return (faded << 24) | (baseColor & 0x00FFFFFF);
    }
}