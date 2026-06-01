package dev.codexbat.vesper.api.glint.type;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A screen-space particle trail that spawns behind the cursor when dragging the
 * registered item through any inventory screen.
 *
 * <p>This glint type does <em>not</em> interact with the 3D item model rendering
 * pipeline. All particle logic is handled by
 * {@code TrailParticleManager} and
 * {@code GlintTrailParticle}.
 *
 * <h2>Example — snow trail on snowball:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.SNOWBALL,
 *     TrailGlint.builder()
 *         .config(ParticleConfig.builder()
 *             .color(0xCCFFFFFF)
 *             .spawnRate(4)
 *             .lifetime(22)
 *             .spread(5.0f)
 *             .gravity(0.04f)
 *             .build())
 *         .build()
 * );
 * }</pre>
 */
@Environment(EnvType.CLIENT)
public final class TrailGlint extends GlintDefinition {

    private final ParticleConfig config;

    private final boolean worldParticles;

    private TrailGlint(Builder b) {
        this.config         = b.config;
        this.worldParticles = b.worldParticles;
    }

    /** Whether particles should appear around this item when held in the world. Defaults {@code false}. */
    public boolean hasWorldParticles() {
        return worldParticles;
    }

    @Override
    public ParticleConfig getWorldConfig() {
        return config;
    }

    @Override
    public GlintCategory category() { return GlintCategory.TRAIL; }

    /** Returns the particle config used for the drag trail. */
    public ParticleConfig getConfig() { return config; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ParticleConfig config  = ParticleConfig.builder().build();
        private boolean worldParticles = false;

        public Builder config(ParticleConfig config) {
            this.config = config;
            return this;
        }
        public Builder worldParticles(boolean world) {
            this.worldParticles = world;
            return this;
        }

        public TrailGlint build() { return new TrailGlint(this); }
    }
}