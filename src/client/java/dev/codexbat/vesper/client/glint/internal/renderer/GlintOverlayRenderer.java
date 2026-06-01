package dev.codexbat.vesper.client.glint.internal.renderer;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import dev.codexbat.vesper.api.glint.type.SpecialGlint;
import dev.codexbat.vesper.api.glint.type.StandardGlint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;

import java.awt.Color;

/**
 * Computes the per-frame glint overlay color for
 * {@link GlintCategory#STANDARD} and
 * {@link GlintCategory#SPECIAL} glints.
 *
 * <p>This class is the integration point for mixin-injected rendering:
 * {ItemStackGlintMixin} calls
 * {@link #getGlintColor} to determine the color before the render system
 * applies the glint texture pass.
 *
 * <h2>How to hook rendering (for future implementors):</h2>
 * <p>When wiring up {@code ItemRenderer} mixins for STANDARD/SPECIAL glint color
 * overrides, call {@code GlintOverlayRenderer.getGlintColor(item)} and apply
 * the returned ARGB to the glint vertex consumer's color multiplier.
 */
@Environment(EnvType.CLIENT)
public final class GlintOverlayRenderer {

    private GlintOverlayRenderer() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the current ARGB glint overlay color for the given item.
     * For {@link SpecialGlint}, the color is animated based on {@link System#currentTimeMillis()}.
     *
     * @param item The item to query.
     * @return An ARGB integer, or {@code -1} if no STANDARD/SPECIAL glint is registered.
     */
    public static int getGlintColor(Item item) {
        return VesperGlintRegistry.getGlint(item).map(def -> switch (def) {
            case StandardGlint sg -> sg.getColor();
            case SpecialGlint sg  -> computeSpecialColor(sg);
            default               -> -1;
        }).orElse(-1);
    }

    /**
     * Returns {@code true} if Vesper should fully replace (suppress) the vanilla glint
     * rendering for this item and handle it internally.
     *
     * @param item The item to check.
     */
    public static boolean suppressVanillaGlint(Item item) {
        return VesperGlintRegistry.getGlint(item).map(def -> switch (def) {
            case StandardGlint ignored -> true;
            case SpecialGlint ignored  -> true;
            default                    -> false;
        }).orElse(false);
    }

    // -------------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------------

    private static int computeSpecialColor(SpecialGlint glint) {
        double time = System.currentTimeMillis();
        return switch (glint.getEffectType()) {

            case PULSE -> {
                // Animate the alpha channel of primaryColor in a sine wave
                float wave      = (float)((Math.sin(time * 0.005 * glint.getSpeed()) + 1.0) / 2.0);
                int baseAlpha   = (glint.getPrimaryColor() >> 24) & 0xFF;
                int pulsedAlpha = (int)(baseAlpha * wave * glint.getIntensity());
                yield (pulsedAlpha << 24) | (glint.getPrimaryColor() & 0x00FFFFFF);
            }

            case SHIMMER -> {
                // Interpolate smoothly between primary and secondary colors
                float t = (float)((Math.sin(time * 0.008 * glint.getSpeed()) + 1.0) / 2.0);
                int blended = lerpColor(glint.getPrimaryColor(), glint.getSecondaryColor(), t);
                yield applyIntensity(blended, glint.getIntensity());
            }

            case RAINBOW -> {
                // Cycle hue across the full spectrum
                float hue  = (float)((time * 0.001 * glint.getSpeed()) % 1.0);
                float alpha = ((glint.getPrimaryColor() >> 24) & 0xFF) / 255f * glint.getIntensity();
                yield hsvToArgb(hue, 1.0f, 1.0f, alpha);
            }

            case OUTLINE ->
                // Static color; outline geometry is drawn by the render mixin
                    applyIntensity(glint.getPrimaryColor(), glint.getIntensity());
        };
    }

    // -------------------------------------------------------------------------
    // Color math helpers
    // -------------------------------------------------------------------------

    /**
     * Linearly interpolates between two ARGB colors.
     *
     * @param a Start color (ARGB).
     * @param b End color (ARGB).
     * @param t Blend factor, {@code 0.0}–{@code 1.0}.
     * @return Interpolated ARGB color.
     */
    public static int lerpColor(int a, int b, float t) {
        int aA = (a >> 24) & 0xFF, aR = (a >> 16) & 0xFF, aG = (a >> 8) & 0xFF, aB = a & 0xFF;
        int bA = (b >> 24) & 0xFF, bR = (b >> 16) & 0xFF, bG = (b >> 8) & 0xFF, bB = b & 0xFF;
        return ((int)(aA + (bA - aA) * t) << 24)
                | ((int)(aR + (bR - aR) * t) << 16)
                | ((int)(aG + (bG - aG) * t) << 8)
                |  (int)(aB + (bB - aB) * t);
    }

    /**
     * Multiplies the alpha channel of an ARGB color by the given intensity factor.
     */
    public static int applyIntensity(int argb, float intensity) {
        int alpha = (int)(((argb >> 24) & 0xFF) * intensity);
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Converts HSV + alpha to an ARGB integer.
     *
     * @param h Hue,        {@code 0.0}–{@code 1.0}
     * @param s Saturation, {@code 0.0}–{@code 1.0}
     * @param v Value,      {@code 0.0}–{@code 1.0}
     * @param a Alpha,      {@code 0.0}–{@code 1.0}
     */
    public static int hsvToArgb(float h, float s, float v, float a) {
        float[] rgb = Color.getHSBColor(h, s, v).getRGBComponents(null);
        return ((int)(a * 255)     << 24)
                | ((int)(rgb[0] * 255) << 16)
                | ((int)(rgb[1] * 255) << 8)
                |  (int)(rgb[2] * 255);
    }
}