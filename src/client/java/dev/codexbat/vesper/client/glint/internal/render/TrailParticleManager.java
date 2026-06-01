package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.type.TrailGlint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the pool of active screen-space {@link GlintTrailParticle}s and drives
 * their tick and render lifecycle.
 *
 * <h2>Lifecycle (called from {HandledScreenMixin}):</h2>
 * <ol>
 *   <li>{@link #spawn} — called each frame while a trail-glint item is held by the cursor.</li>
 *   <li>{@link #tick}  — advances all particle physics by one frame.</li>
 *   <li>{@link #render} — draws all alive particles to the screen.</li>
 * </ol>
 *
 * <p>All operations are single-threaded and must be called on the render thread.
 */
@Environment(EnvType.CLIENT)
public final class TrailParticleManager {

    /** Hard cap on concurrent live particles to prevent perf spikes with many registered items. */
    private static final int MAX_PARTICLES = 768;
    private static final List<GlintTrailParticle> PARTICLES = new ArrayList<>(64);

    private static long lastTickMs = 0;
    private static final long STALE_THRESHOLD_MS = 500;

    private TrailParticleManager() {}

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Spawns particles from the given cursor position based on a trail definition.
     * Call once per render frame while the cursor holds a trail-glint item.
     *
     * @param cursorX    Screen X of the cursor (typically {@code mouseX} from the render method).
     * @param cursorY    Screen Y of the cursor (typically {@code mouseY}).
     * @param definition The {@link TrailGlint} defining particle behavior.
     */
    public static void spawn(double cursorX, double cursorY, TrailGlint definition) {
        if (PARTICLES.size() >= MAX_PARTICLES) return;
        int toSpawn = Math.min(definition.getConfig().getSpawnRate(), MAX_PARTICLES - PARTICLES.size());
        for (int i = 0; i < toSpawn; i++) {
            PARTICLES.add(new GlintTrailParticle(cursorX, cursorY, definition.getConfig()));
        }
    }

    /**
     * Updates all live particles by one frame: moves them, applies gravity/drag, and
     * removes any that have expired.
     * <p>Call this exactly once per render frame, after {@link #spawn}.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        if (lastTickMs > 0 && now - lastTickMs > STALE_THRESHOLD_MS) {
            PARTICLES.clear(); // stale burst guard
        }
        lastTickMs = now;

        Iterator<GlintTrailParticle> it = PARTICLES.iterator();
        while (it.hasNext()) {
            GlintTrailParticle p = it.next();
            p.update();
            if (p.isDead()) it.remove();
        }
    }

    /**
     * Renders all live particles using the provided {@link DrawContext}.
     * <p>Should be called during the GUI render phase, ideally after all other
     * screen content but before tooltips, so particles appear on top of slots
     * but below the tooltip layer.
     *
     * <p>{@code DrawContext.fill()} internally routes through {@code RenderPipelines.GUI},
     * which already declares alpha blending in its pipeline descriptor. No manual
     * blend or depth state setup is needed — those RenderSystem methods no longer
     * exist in this version of Minecraft.
     *
     * @param context The draw context for the current frame.
     */
    public static void render(DrawContext context) {
        if (PARTICLES.isEmpty()) return;

        for (GlintTrailParticle p : PARTICLES) {
            renderParticle(context, p);
        }
    }

    /**
     * Clears all active particles immediately.
     * Should be called when closing an inventory screen.
     */
    public static void clear() {
        PARTICLES.clear();
    }

    /** @return The number of currently live particles (useful for debug overlays). */
    public static int getParticleCount() {
        return PARTICLES.size();
    }

    // -------------------------------------------------------------------------
    // Internal rendering
    // -------------------------------------------------------------------------

    private static void renderParticle(DrawContext context, GlintTrailParticle p) {
        int color = p.getCurrentColor();
        int r     = Math.max(1, (int) p.size);
        int cx    = (int) p.x;
        int cy    = (int) p.y;

        switch (p.shape) {
            case SQUARE  -> context.fill(cx, cy, cx + r, cy + r, color);
            case CIRCLE  -> renderCircle(context, cx, cy, r, color);
            case CROSS   -> renderCross(context, cx, cy, r, color);
            case DIAMOND -> renderDiamond(context, cx, cy, r, color);
        }
    }

    /**
     * Renders a filled diamond (rotated square) centered at (cx, cy) with radius r.
     */
    private static void renderDiamond(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = r - Math.abs(dy);
            context.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, color);
        }
    }

    /**
     * Renders a plus-shaped cross centered at (cx, cy) with arm length r.
     */
    private static void renderCross(DrawContext context, int cx, int cy, int r, int color) {
        context.fill(cx - r, cy,     cx + r + 1, cy + 1,     color); // horizontal
        context.fill(cx,     cy - r, cx + 1,     cy + r + 1, color); // vertical
    }

    /**
     * Renders an approximate filled circle using horizontal scan lines.
     */
    private static void renderCircle(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.sqrt((double) r * r - (double) dy * dy);
            context.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, color);
        }
    }
}