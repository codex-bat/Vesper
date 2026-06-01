package dev.codexbat.vesper.api.glint.type;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Screen-space idle sparkle glint: particles continuously appear and fade around an
 * item sitting in a slot or the hotbar — no dragging, no trail.
 *
 * <p>For a combined idle-sparkle + drag-trail effect, use
 * {@link SparkleGlint} instead.
 *
 * <h2>Example — subtle amethyst sparkle:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.AMETHYST_SHARD,
 *     AmbientGlint.builder()
 *         .config(ParticleConfig.builder()
 *             .color(0xBBCC88FF)
 *             .spawnRate(1).lifetime(40).spread(7.0f)
 *             .shape(ParticleConfig.Shape.DIAMOND).fadeOut(true).build())
 *         .build()
 * );
 * }</pre>
 *
 * @code AmbientParticleManager
 * @code GlintAmbientParticle
 * @see SparkleGlint
 */
@Environment(EnvType.CLIENT)
public final class AmbientGlint extends GlintDefinition {

    private final ParticleConfig config;

    private final boolean worldParticles;

    private AmbientGlint(Builder b) {
        this.config = b.config;
        this.worldParticles = b.worldParticles;
    }

    @Override
    public GlintCategory category() { return GlintCategory.AMBIENT; }

    public boolean hasWorldParticles() {
        return worldParticles;
    }

    @Override
    public ParticleConfig getWorldConfig() {
        return config;
    }

    /** Returns the particle config used for idle sparkles. */
    public ParticleConfig getConfig() { return config; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ParticleConfig config = ParticleConfig.builder().build();
        private boolean worldParticles = false; // opt-in

        public Builder config(ParticleConfig config) {
            this.config = config;
            return this;
        }
        public Builder worldParticles(boolean world) {
            this.worldParticles = world;
            return this;
        }

        public AmbientGlint build() { return new AmbientGlint(this); }
    }
}