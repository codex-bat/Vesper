package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.ParticleConfig;
import dev.codexbat.vesper.api.glint.type.TrailGlint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Random;

/**
 * Represents a single live, screen-space particle spawned by a {@link TrailGlint}.
 *
 * <p>Particles are owned and updated exclusively by {@link TrailParticleManager}.
 * Do not hold external references to particle instances; treat them as ephemeral.
 */
@Environment(EnvType.CLIENT)
public final class GlintTrailParticle {

    private static final Random RANDOM = new Random();

    // Position and motion (screen pixels)
    public double x;
    public double y;
    public double velX;
    public double velY;

    // Lifetime
    public int lifetime;
    public final int maxLifetime;

    // Appearance
    public final int baseColor;   // ARGB
    public final float size;
    public final ParticleConfig.Shape shape;
    public final boolean fadeOut;
    private final float gravity;

    /**
     * Constructs a particle at the given screen origin using the provided {@link TrailGlint} definition.
     * Position, velocity, and size are randomized within the definition's configured ranges.
     *
     * @param originX Screen X of the cursor.
     * @param originY Screen Y of the cursor.
     * @param config The trail definition to source parameters from.
     */
    public GlintTrailParticle(double originX, double originY, ParticleConfig config) {
        this.maxLifetime = config.getLifetime();
        this.lifetime    = this.maxLifetime;
        this.baseColor   = config.getColor();
        this.shape       = config.getShape();
        this.fadeOut     = config.isFadeOut();
        this.gravity     = config.getGravity();
        this.size        = config.getSizeMin()
                + RANDOM.nextFloat() * (config.getSizeMax() - config.getSizeMin());

        float spread = config.getSpread();
        this.x = originX + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;
        this.y = originY + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;

        float speed = config.getSpeed();
        this.velX = (RANDOM.nextFloat() - 0.5f) * speed;
        this.velY = (RANDOM.nextFloat() - 0.5f) * speed - speed * 0.25f;
    }

    /**
     * Advances this particle by one frame: applies velocity, gravity, drag, and decrements lifetime.
     */
    public void update() {
        x    += velX;
        y    += velY;
        velY += gravity;        // downward pull
        velX *= 0.94f;          // horizontal drag
        velY *= 0.97f;          // vertical drag (slightly less, to preserve gravity feel)
        lifetime--;
    }

    /** @return {@code true} when this particle has expired and should be removed. */
    public boolean isDead() {
        return lifetime <= 0;
    }

    /**
     * Returns the current ARGB color, with alpha faded proportionally to remaining lifetime
     * if {@link ParticleConfig#isFadeOut()} is enabled.
     */
    public int getCurrentColor() {
        if (!fadeOut) return baseColor;
        float ratio    = (float) lifetime / maxLifetime;
        int baseAlpha  = (baseColor >> 24) & 0xFF;
        int fadedAlpha = (int)(baseAlpha * ratio);
        return (fadedAlpha << 24) | (baseColor & 0x00FFFFFF);
    }
}