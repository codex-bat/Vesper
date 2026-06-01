package dev.codexbat.vesper.api.glint.type;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

/**
 * A standard scrolling glint overlay, analogous to the vanilla enchantment glow.
 *
 * <p>Renders as a tiled, scrolling texture over the item model. Supports a custom
 * ARGB tint color, optional texture override, scroll speed, and opacity.
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.NETHERITE_SWORD,
 *     StandardGlint.builder()
 *         .color(0xFFFF4400)   // fiery orange tint
 *         .speed(1.5f)
 *         .intensity(0.8f)
 *         .build()
 * );
 * }</pre>
 */
@Environment(EnvType.CLIENT)
public final class StandardGlint extends GlintDefinition {

    private final int color;
    private final Identifier texture;
    private final float speed;
    private final float intensity;

    private StandardGlint(Builder builder) {
        this.color     = builder.color;
        this.texture   = builder.texture;
        this.speed     = builder.speed;
        this.intensity = builder.intensity;
    }

    @Override
    public GlintCategory category() { return GlintCategory.STANDARD; }

    /** ARGB color applied as a tint over the glint texture. */
    public int getColor()           { return color; }

    /**
     * Custom glint texture identifier, or {@code null} to use the vanilla glint texture
     * ({@code textures/misc/enchanted_item_glint.png}).
     */
    public Identifier getTexture()  { return texture; }

    /** Scroll animation speed multiplier. {@code 1.0} = vanilla speed. */
    public float getSpeed()         { return speed; }

    /** Glint opacity, {@code 0.0}–{@code 1.0}. */
    public float getIntensity()     { return intensity; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int color          = 0xFF6040FF; // default: vanilla-ish purple
        private Identifier texture = null;
        private float speed        = 1.0f;
        private float intensity    = 1.0f;

        /**
         * ARGB tint color for the glint overlay.
         * The alpha channel controls the base opacity before {@link #intensity} is applied.
         */
        public Builder color(int color)             { this.color = color;         return this; }

        /**
         * Custom texture identifier. Pass {@code null} to use vanilla's enchantment glint.
         * The texture must be registered as a game resource.
         */
        public Builder texture(Identifier texture)  { this.texture = texture;     return this; }

        /** Scroll animation speed multiplier. Values above {@code 1.0} speed up the glint. */
        public Builder speed(float speed)           { this.speed = speed;         return this; }

        /** Overlay opacity in range {@code 0.0}–{@code 1.0}. */
        public Builder intensity(float intensity)   { this.intensity = intensity; return this; }

        public StandardGlint build()                { return new StandardGlint(this); }
    }
}