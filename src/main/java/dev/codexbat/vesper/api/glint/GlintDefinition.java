package dev.codexbat.vesper.api.glint;

import dev.codexbat.vesper.api.glint.type.SpecialGlint;
import dev.codexbat.vesper.api.glint.type.StandardGlint;
import dev.codexbat.vesper.api.glint.type.TrailGlint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Abstract base for all glint definitions registered with {@link VesperGlintRegistry}.
 *
 * <p>A {@code GlintDefinition} is an immutable, builder-produced descriptor that tells
 * Vesper's rendering layer <em>how</em> to visually modify an item. It does not hold any
 * mutable render state; all live state lives in the renderer/particle manager.
 *
 * <h2>Extending</h2>
 * <p>Concrete subclasses must implement {@link #category()} to declare which rendering
 * pipeline owns them, and should expose their parameters as plain getter methods:
 *
 * <pre>{@code
 * public final class MyCustomGlint extends GlintDefinition {
 *
 *     private final int color;
 *
 *     private MyCustomGlint(Builder b) { this.color = b.color; }
 *
 *     @Override public GlintCategory category() { return GlintCategory.STANDARD; }
 *
 *     public int getColor() { return color; }
 *
 *     public static Builder builder() { return new Builder(); }
 *
 *     public static final class Builder {
 *         private int color = 0xFFFFFFFF;
 *         public Builder color(int color) { this.color = color; return this; }
 *         public MyCustomGlint build()    { return new MyCustomGlint(this); }
 *     }
 * }
 * }</pre>
 *
 * <h2>Registration</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.DIAMOND_SWORD,
 *     MyCustomGlint.builder().color(0xFF00FFFF).build()
 * );
 * }</pre>
 *
 * <p>Subclasses should be annotated with {@code @Environment(EnvType.CLIENT)} because
 * glint definitions are a purely client-side concept.
 *
 * @see GlintCategory
 * @see VesperGlintRegistry
 * @see StandardGlint
 * @see SpecialGlint
 * @see TrailGlint
 */
@Environment(EnvType.CLIENT)
public abstract class GlintDefinition {

    /**
     * Protected constructor — only subclasses (and their builders) should instantiate.
     */
    protected GlintDefinition() {}

    // -------------------------------------------------------------------------
    // Contract
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link GlintCategory} this definition belongs to.
     *
     * <p>The category determines which rendering subsystem processes this glint:
     * <ul>
     *   <li>{@link GlintCategory#STANDARD} / {@link GlintCategory#SPECIAL}
     *       → {@code GlintOverlayRenderer} (3-D model overlay)</li>
     *   <li>{@link GlintCategory#TRAIL}
     *       → {@code TrailParticleManager} (inventory screen particles)</li>
     * </ul>
     *
     * @return The category; never {@code null}.
     */
    public abstract GlintCategory category();

    // -------------------------------------------------------------------------
    // Convenience helpers (final — not part of the subclass contract)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this definition belongs to {@link GlintCategory#STANDARD}.
     */
    public final boolean isStandard() {
        return category() == GlintCategory.STANDARD;
    }

    /**
     * Returns {@code true} if this definition belongs to {@link GlintCategory#SPECIAL}.
     */
    public final boolean isSpecial() {
        return category() == GlintCategory.SPECIAL;
    }

    /**
     * Returns {@code true} if this definition belongs to {@link GlintCategory#TRAIL}.
     */
    public final boolean isTrail() {
        return category() == GlintCategory.TRAIL;
    }

    /**
     * Returns {@code true} if this definition belongs to either
     * {@link GlintCategory#STANDARD} or {@link GlintCategory#SPECIAL} —
     * i.e. it is handled by the 3-D overlay renderer rather than the
     * screen-space particle system.
     */
    public final boolean isOverlay() {
        GlintCategory cat = category();
        return cat == GlintCategory.STANDARD || cat == GlintCategory.SPECIAL;
    }

    /**
     * Whether this glint should emit particles in the real game world.
     *
     * <p>Examples:
     * <ul>
     *     <li>Dropped item entities</li>
     *     <li>Held weapons/tools</li>
     *     <li>Armor pieces</li>
     * </ul>
     */
    public boolean hasWorldParticles() {
        return false;
    }

    /**
     * Particle configuration used for world-space particles.
     *
     * <p>Only queried when {@link #hasWorldParticles()} returns true.
     */
    public ParticleConfig getWorldConfig() {
        return null;
    }

    // -------------------------------------------------------------------------
    // Object
    // -------------------------------------------------------------------------

    /**
     * Returns a concise human-readable description useful for logging.
     * Subclasses may override this to include their own parameters.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[category=" + category().name().toLowerCase() + "]";
    }
}