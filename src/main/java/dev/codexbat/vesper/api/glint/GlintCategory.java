package dev.codexbat.vesper.api.glint;

import dev.codexbat.vesper.api.glint.type.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Broad classification for a {@link GlintDefinition}.
 * Determines which rendering pipeline handles the glint.
 *
 * <table border="1">
 *   <tr><th>Category</th><th>Render context</th><th>Implemented by</th></tr>
 *   <tr><td>{@link #STANDARD}</td><td>3-D model overlay</td><td>{@link StandardGlint}</td></tr>
 *   <tr><td>{@link #SPECIAL}</td><td>3-D model overlay (animated)</td><td>{@link SpecialGlint}</td></tr>
 *   <tr><td>{@link #TRAIL}</td><td>Screen-space, cursor-drag only</td><td>{@link TrailGlint}</td></tr>
 *   <tr><td>{@link #AMBIENT}</td><td>Screen-space, idle slot/hotbar only</td><td>{@link AmbientGlint}</td></tr>
 *   <tr><td>{@link #SPARKLE}</td><td>Screen-space, idle + drag trail</td><td>{@link SparkleGlint}</td></tr>
 * </table>
 */
@Environment(EnvType.CLIENT)
public enum GlintCategory {

    /**
     * Colored scrolling overlay — vanilla-style glint with custom ARGB tint.
     * Implemented by {@link StandardGlint}.
     */
    STANDARD,

    /**
     * Animated effect glints: pulse, shimmer, rainbow cycling, outline.
     * Implemented by {@link SpecialGlint}.
     */
    SPECIAL,

    /**
     * Screen-space particle trail — only active while the item is being dragged
     * in an inventory screen.
     * Implemented by {@link TrailGlint}.
     */
    TRAIL,

    /**
     * Screen-space idle sparkles — particles appear around the item while it
     * sits still in a slot or the hotbar. No drag trail.
     * Implemented by {@link AmbientGlint}.
     */
    AMBIENT,

    /**
     * Screen-space idle sparkles <em>plus</em> a drag trail — combines both
     * effects with optionally different particle configs for each.
     * Implemented by {@link SparkleGlint}.
     */
    SPARKLE
}