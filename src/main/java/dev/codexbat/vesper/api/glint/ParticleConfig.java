package dev.codexbat.vesper.api.glint;

import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Shared, immutable descriptor for a set of particle appearance and physics parameters.
 *
 * <p>Used by both {@link AmbientGlint} (idle sparkles)
 * and {@link SparkleGlint} (idle + trail combined),
 * as well as by {@code GlintAmbientParticle}
 * and {@code AmbientParticleManager}.
 *
 * <p>Extracting this class means future glint types that use screen-space particles
 * can share the same config vocabulary without coupling to a specific glint type.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * ParticleConfig config = ParticleConfig.builder()
 *     .color(0xAAFFFFFF)
 *     .spawnRate(1)
 *     .lifetime(35)
 *     .spread(6.0f)
 *     .gravity(0.02f)
 *     .sizeMin(1.0f).sizeMax(2.0f)
 *     .shape(ParticleConfig.Shape.SQUARE)
 *     .fadeOut(true)
 *     .build();
 * }</pre>
 */
@Environment(EnvType.CLIENT)
public final class ParticleConfig {

    // -------------------------------------------------------------------------
    // Shape enum
    // -------------------------------------------------------------------------

    /** Pixel shape drawn for each individual particle. */
    public enum Shape {
        /** Solid filled square. Fast to render, good default. */
        SQUARE,
        /** Solid filled circle (midpoint approximation). */
        CIRCLE,
        /** Plus-shaped cross, one pixel wide. */
        CROSS,
        /** Diamond (rotated square) silhouette. */
        DIAMOND
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final int color;
    private final int spawnRate;
    private final float speed;
    private final int lifetime;
    private final float spread;
    private final float gravity;
    private final float sizeMin;
    private final float sizeMax;
    private final Shape shape;
    private final boolean fadeOut;

    private ParticleConfig(Builder b) {
        this.color     = b.color;
        this.spawnRate = b.spawnRate;
        this.speed     = b.speed;
        this.lifetime  = b.lifetime;
        this.spread    = b.spread;
        this.gravity   = b.gravity;
        this.sizeMin   = b.sizeMin;
        this.sizeMax   = b.sizeMax;
        this.shape     = b.shape;
        this.fadeOut   = b.fadeOut;
    }

    /** Base ARGB color. Alpha is respected and will be faded further if {@link #isFadeOut()} is true. */
    public int getColor()       { return color; }
    /** Particles spawned per render frame. */
    public int getSpawnRate()   { return spawnRate; }
    /** Base velocity magnitude. */
    public float getSpeed()     { return speed; }
    /** Particle lifetime in render frames (~20 frames ≈ 1 second at 20fps). */
    public int getLifetime()    { return lifetime; }
    /** Spawn scatter radius around the origin in screen pixels. */
    public float getSpread()    { return spread; }
    /** Downward acceleration applied per frame (positive = falls down). */
    public float getGravity()   { return gravity; }
    /** Minimum particle pixel size (randomly chosen per particle). */
    public float getSizeMin()   { return sizeMin; }
    /** Maximum particle pixel size. */
    public float getSizeMax()   { return sizeMax; }
    /** Pixel shape of each particle. */
    public Shape getShape()     { return shape; }
    /** Whether alpha fades to zero as lifetime expires. */
    public boolean isFadeOut()  { return fadeOut; }

    public static Builder builder() { return new Builder(); }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static final class Builder {
        private int color           = 0xFFFFFFFF;
        private int spawnRate       = 1;
        private float speed         = 0.4f;
        private int lifetime        = 30;
        private float spread        = 6.0f;
        private float gravity       = 0.02f;
        private float sizeMin       = 1.0f;
        private float sizeMax       = 2.5f;
        private Shape shape         = Shape.SQUARE;
        private boolean fadeOut     = true;

        public Builder color(int color)         { this.color = color;       return this; }
        public Builder spawnRate(int rate)      { this.spawnRate = rate;    return this; }
        public Builder speed(float speed)       { this.speed = speed;       return this; }
        public Builder lifetime(int lifetime)   { this.lifetime = lifetime; return this; }
        public Builder spread(float spread)     { this.spread = spread;     return this; }
        public Builder gravity(float gravity)   { this.gravity = gravity;   return this; }
        public Builder sizeMin(float min)       { this.sizeMin = min;       return this; }
        public Builder sizeMax(float max)       { this.sizeMax = max;       return this; }
        public Builder shape(Shape shape)       { this.shape = shape;       return this; }
        public Builder fadeOut(boolean fadeOut) { this.fadeOut = fadeOut;   return this; }

        public ParticleConfig build() { return new ParticleConfig(this); }
    }
}