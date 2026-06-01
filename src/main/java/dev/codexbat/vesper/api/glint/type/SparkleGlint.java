package dev.codexbat.vesper.api.glint.type;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Combined idle-sparkle + drag-trail glint.
 *
 * <p>Particles continuously appear around the item while it sits in a slot or the
 * hotbar (<em>idle pass</em>), and a separate or matching particle effect trails
 * behind when the item is picked up and dragged in an inventory screen
 * (<em>trail pass</em>).
 *
 * <h2>Shared vs. separate trail</h2>
 * <p>When {@link #isSharedParticle()} is {@code true} (the default), the same
 * {@link ParticleConfig} drives both passes — convenient for a consistent look.
 * When {@code false}, supply a second config via {@link Builder#trail} so you can
 * have, for example, subtle stars at rest and heavier snowfall while dragging.
 *
 * <h2>Example — snowball: gentle idle snowfall + heavier drag trail:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.SNOWBALL,
 *     SparkleGlint.builder()
 *         .idle(ParticleConfig.builder()
 *             .color(0xAAFFFFFF).spawnRate(1).lifetime(35).spread(6.0f)
 *             .gravity(0.02f).sizeMin(1.0f).sizeMax(2.0f)
 *             .shape(ParticleConfig.Shape.SQUARE).fadeOut(true).build())
 *         .sharedParticle(false)
 *         .trail(ParticleConfig.builder()
 *             .color(0xCCFFFFFF).spawnRate(4).lifetime(22).spread(5.0f)
 *             .gravity(0.04f).sizeMin(1.5f).sizeMax(3.5f)
 *             .shape(ParticleConfig.Shape.SQUARE).fadeOut(true).build())
 *         .build()
 * );
 * }</pre>
 *
 * <h2>Example — amethyst shard: same sparkle idle and dragged:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.AMETHYST_SHARD,
 *     SparkleGlint.builder()
 *         .idle(ParticleConfig.builder()
 *             .color(0xBBCC88FF).spawnRate(1).lifetime(40).spread(7.0f)
 *             .shape(ParticleConfig.Shape.DIAMOND).fadeOut(true).build())
 *         .sharedParticle(true)   // trail mirrors idle, no .trail() needed
 *         .build()
 * );
 * }</pre>
 *
 * @see AmbientGlint for idle-only (no trail).
 * @code AmbientParticleManager
 * @code GlintAmbientParticle
 */
@Environment(EnvType.CLIENT)
public final class SparkleGlint extends GlintDefinition {

    private final ParticleConfig idleConfig;
    private final boolean sharedParticle;
    private final ParticleConfig trailConfig; // always non-null after build()

    private final boolean worldParticles;
    private final boolean worldUseTrailConfig;

    private SparkleGlint(Builder b) {
        this.idleConfig          = b.idleConfig;
        this.sharedParticle      = b.sharedParticle;
        this.trailConfig         = b.trailConfig;
        this.worldParticles      = b.worldParticles;
        this.worldUseTrailConfig = b.worldUseTrailConfig;
    }

    /** Whether particles should appear around this item when held/dropped in the world. */
    public boolean hasWorldParticles() { return worldParticles; }

    /**
     * Returns the config to use for world-space particles.
     * Returns {@link #getTrailConfig()} if {@code worldUseTrailConfig} was set and a
     * separate trail config exists; otherwise returns {@link #getIdleConfig()}.
     */
    public ParticleConfig getWorldConfig() {
        return (worldUseTrailConfig && !sharedParticle) ? trailConfig : idleConfig;
    }

    @Override
    public GlintCategory category() { return GlintCategory.SPARKLE; }

    /** Returns the {@link ParticleConfig} used for idle (stationary-slot) particles. */
    public ParticleConfig getIdleConfig() { return idleConfig; }

    /**
     * Returns {@code true} if the drag trail reuses {@link #getIdleConfig()}.
     * Returns {@code false} if a separate trail config was provided via {@link Builder#trail}.
     */
    public boolean isSharedParticle() { return sharedParticle; }

    /**
     * Returns the {@link ParticleConfig} for the drag trail.
     * Always returns {@link #getIdleConfig()} when {@link #isSharedParticle()} is {@code true}.
     */
    public ParticleConfig getTrailConfig() {
        return sharedParticle ? idleConfig : trailConfig;
    }

    public static Builder builder() { return new Builder(); }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static final class Builder {
        private ParticleConfig idleConfig  = ParticleConfig.builder().build();
        private boolean sharedParticle     = true;
        private ParticleConfig trailConfig = null;

        private boolean worldParticles      = false;
        private boolean worldUseTrailConfig = false; // when true, getWorldConfig() returns trailConfig

        public Builder worldParticles(boolean world)         { this.worldParticles = world;           return this; }
        /**
         * When {@code true}, world-space particles use the trail config instead of idle.
         * Only meaningful when {@link #sharedParticle} is {@code false}.
         */
        public Builder worldUseTrailConfig(boolean useTrail) { this.worldUseTrailConfig = useTrail;   return this; }

        /**
         * Sets the particle config for idle (stationary) sparkles.
         * Required — all other settings have sensible defaults.
         */
        public Builder idle(ParticleConfig config) {
            this.idleConfig = config;
            return this;
        }

        /**
         * When {@code true} (default), the drag trail reuses the idle config.
         * When {@code false}, call {@link #trail} to define a separate trail config.
         */
        public Builder sharedParticle(boolean shared) {
            this.sharedParticle = shared;
            return this;
        }

        /**
         * Sets the particle config for the drag trail.
         * Only takes effect when {@link #sharedParticle(boolean)} is {@code false}.
         */
        public Builder trail(ParticleConfig config) {
            this.trailConfig = config;
            return this;
        }

        public SparkleGlint build() {
            if (!sharedParticle && trailConfig == null) {
                trailConfig = idleConfig; // graceful fallback, no crash at registration time
            }
            if (trailConfig == null) trailConfig = idleConfig;
            return new SparkleGlint(this);
        }
    }
}