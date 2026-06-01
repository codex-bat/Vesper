package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the pool of active {@link GlintAmbientParticle}s.
 *
 * <p>Used by both {@link AmbientGlint}
 * (idle-only) and {@link SparkleGlint}
 * (idle + trail). The manager itself is agnostic about the source — it just
 * receives a {@link ParticleConfig} and an origin point.
 *
 * <h2>Lifecycle (driven externally by mixins):</h2>
 * <ol>
 *   <li>{@link #spawn} — called each frame for each origin that should emit particles.</li>
 *   <li>{@link #tick}  — advances physics; called once per frame.</li>
 *   <li>{@link #render} — draws all live particles.</li>
 *   <li>{@link #clear} — on screen close or scene change.</li>
 * </ol>
 *
 * <p>The {@code HandledScreenMixin} and {@code InGameHudMixin} are the two callers.
 * Only one should tick/render per frame — the HUD mixin guards against double-ticking
 * when an inventory screen is open.
 */
@Environment(EnvType.CLIENT)
public final class AmbientParticleManager {

    private static final int MAX_PARTICLES = 1024;
    private static final List<GlintAmbientParticle> PARTICLES = new ArrayList<>(128);

    private static long lastTickMs = 0;
    private static final long STALE_THRESHOLD_MS = 500;

    private AmbientParticleManager() {}

    // -------------------------------------------------------------------------
    // Spawn
    // -------------------------------------------------------------------------

    /**convenience delegate*/
    public static void spawn(double originX, double originY, ParticleConfig config) {
        spawn(originX, originY, config, 1.0f);
    }

    /**
     * Spawns particles with a pre-applied alpha multiplier baked into each particle.
     * Use this when the spawn origin sits behind an overlay (e.g. hotbar under an
     * open inventory screen) so the particles don't bleed through at full brightness.
     *
     * @param alphaMultiplier {@code 1.0f} = full opacity; {@code 0.3f} = 30 % opacity.
     */
    public static void spawn(double originX, double originY, ParticleConfig config,
                             float alphaMultiplier) {
        if (PARTICLES.size() >= MAX_PARTICLES) return;
        int toSpawn = Math.min(config.getSpawnRate(), MAX_PARTICLES - PARTICLES.size());
        for (int i = 0; i < toSpawn; i++) {
            PARTICLES.add(new GlintAmbientParticle(originX, originY, config, alphaMultiplier));
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /** Advances all particles by one frame and removes expired ones. */
    public static void tick() {
        long now = System.currentTimeMillis();
        if (lastTickMs > 0 && now - lastTickMs > STALE_THRESHOLD_MS) {
            PARTICLES.clear(); // stale burst guard
        }
        lastTickMs = now;

        Iterator<GlintAmbientParticle> it = PARTICLES.iterator();
        while (it.hasNext()) {
            GlintAmbientParticle p = it.next();
            p.update();
            if (p.isDead()) it.remove();
        }
    }

    /** Draws all live particles into the given draw context. */
    public static void render(DrawContext context) {
        if (PARTICLES.isEmpty()) return;
        for (GlintAmbientParticle p : PARTICLES) {
            renderParticle(context, p);
        }
    }

    /** Removes all live particles immediately. Call on screen close or scene change. */
    public static void clear() { PARTICLES.clear(); }

    /** @return Number of currently live particles. */
    public static int getParticleCount() { return PARTICLES.size(); }

    // -------------------------------------------------------------------------
    // Internal rendering
    // -------------------------------------------------------------------------

    private static void renderParticle(DrawContext context, GlintAmbientParticle p) {
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

    private static void renderDiamond(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = r - Math.abs(dy);
            context.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, color);
        }
    }

    private static void renderCross(DrawContext context, int cx, int cy, int r, int color) {
        context.fill(cx - r, cy,     cx + r + 1, cy + 1,     color);
        context.fill(cx,     cy - r, cx + 1,     cy + r + 1, color);
    }

    private static void renderCircle(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.sqrt((double) r * r - (double) dy * dy);
            context.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, color);
        }
    }
}