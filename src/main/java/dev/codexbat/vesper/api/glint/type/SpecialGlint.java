package dev.codexbat.vesper.api.glint.type;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A unique, time-animated glint with configurable visual behavior beyond a simple overlay.
 *
 * <p>The color and appearance are computed each frame by
 * {@code GlintOverlayRenderer} based on the
 * selected {@link EffectType}.
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.TOTEM_OF_UNDYING,
 *     SpecialGlint.builder()
 *         .effectType(SpecialGlint.EffectType.PULSE)
 *         .primaryColor(0xFFFFD700)    // gold
 *         .secondaryColor(0xFFFF6600)  // orange (used as pulse target)
 *         .speed(2.0f)
 *         .build()
 * );
 * }</pre>
 */
@Environment(EnvType.CLIENT)
public final class SpecialGlint extends GlintDefinition {

    /** Defines how the glint animates over time. */
    public enum EffectType {
        /**
         * The glint opacity pulses in and out sinusoidally.
         * Uses {@code primaryColor}; alpha channel is animated.
         */
        PULSE,

        /**
         * The color interpolates back and forth between {@code primaryColor}
         * and {@code secondaryColor} in a smooth wave.
         */
        SHIMMER,

        /**
         * Cycles through the full HSV color spectrum continuously.
         * {@code primaryColor}'s alpha channel is used for opacity.
         */
        RAINBOW,

        /**
         * An animated colored border rendered around the item's bounding sprite.
         * Uses {@code primaryColor}.
         */
        OUTLINE
    }

    private final EffectType effectType;
    private final int primaryColor;
    private final int secondaryColor;
    private final float speed;
    private final float intensity;

    private SpecialGlint(Builder builder) {
        this.effectType     = builder.effectType;
        this.primaryColor   = builder.primaryColor;
        this.secondaryColor = builder.secondaryColor;
        this.speed          = builder.speed;
        this.intensity      = builder.intensity;
    }

    @Override
    public GlintCategory category() { return GlintCategory.SPECIAL; }

    /** The animation mode this glint uses. */
    public EffectType getEffectType()   { return effectType; }

    /** Primary ARGB color. Role varies by {@link EffectType}. */
    public int getPrimaryColor()        { return primaryColor; }

    /**
     * Secondary ARGB color. Used as the interpolation target for
     * {@link EffectType#SHIMMER}; ignored by other modes.
     */
    public int getSecondaryColor()      { return secondaryColor; }

    /** Animation speed multiplier. {@code 1.0} = baseline speed. */
    public float getSpeed()             { return speed; }

    /** Overall overlay opacity multiplier, {@code 0.0}–{@code 1.0}. */
    public float getIntensity()         { return intensity; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private EffectType effectType = EffectType.PULSE;
        private int primaryColor      = 0xFFFFFFFF;
        private int secondaryColor    = 0xFF000000;
        private float speed           = 1.0f;
        private float intensity       = 1.0f;

        /** Animation mode. Defaults to {@link EffectType#PULSE}. */
        public Builder effectType(EffectType type)      { this.effectType = type;        return this; }

        /** Primary ARGB color. */
        public Builder primaryColor(int color)          { this.primaryColor = color;     return this; }

        /** Secondary ARGB color (used by {@link EffectType#SHIMMER}). */
        public Builder secondaryColor(int color)        { this.secondaryColor = color;   return this; }

        /** Animation speed multiplier. */
        public Builder speed(float speed)               { this.speed = speed;            return this; }

        /** Overlay opacity, {@code 0.0}–{@code 1.0}. */
        public Builder intensity(float intensity)       { this.intensity = intensity;    return this; }

        public SpecialGlint build()                     { return new SpecialGlint(this); }
    }
}