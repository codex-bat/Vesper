package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the pool of active {@link GlintSparkleParticle}s for
 * {@link SparkleGlint}.
 *
 * <p>Callers are responsible for choosing which config to pass:
 * <ul>
 *   <li>Slot/hotbar origin → {@code sparkleGlint.getIdleConfig()}</li>
 *   <li>Cursor origin       → {@code sparkleGlint.getTrailConfig()}</li>
 * </ul>
 *
 * <h2>Lifecycle (driven externally by mixins):</h2>
 * <ol>
 *   <li>{@link #spawn}  — called each frame per origin that should emit.</li>
 *   <li>{@link #tick}   — advances physics; called once per frame.</li>
 *   <li>{@link #render} — draws all live particles.</li>
 *   <li>{@link #clear}  — on screen close or scene change.</li>
 * </ol>
 *
 * <p>{@code HandledScreenMixin} and {@code InGameHudMixin} are the two callers.
 * The HUD mixin guards against double-ticking when an inventory screen is open.
 */
@Environment(EnvType.CLIENT)
public final class SparkleParticleManager {

    private static final int MAX_PARTICLES = 1024;
    private static final List<GlintSparkleParticle> PARTICLES = new ArrayList<>(128);

    private static long lastTickMs = 0;
    private static final long STALE_THRESHOLD_MS = 500;

    private SparkleParticleManager() {}

    // -------------------------------------------------------------------------
    // Spawn
    // -------------------------------------------------------------------------

    /**
     * Spawns particles at the given origin using the provided config.
     *
     * @param originX Screen X of the spawn centre.
     * @param originY Screen Y of the spawn centre.
     * @param config  {@link ParticleConfig} controlling appearance and physics.
     *                Pass {@code sparkleGlint.getIdleConfig()} for slot/hotbar origins,
     *                or {@code sparkleGlint.getTrailConfig()} for cursor origins.
     */
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
            PARTICLES.add(new GlintSparkleParticle(originX, originY, config, alphaMultiplier));
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

        Iterator<GlintSparkleParticle> it = PARTICLES.iterator();
        while (it.hasNext()) {
            GlintSparkleParticle p = it.next();
            p.update();
            if (p.isDead()) it.remove();
        }
    }

    /** Draws all live particles into the given draw context. */
    public static void render(DrawContext context) {
        if (PARTICLES.isEmpty()) return;
        for (GlintSparkleParticle p : PARTICLES) {
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

    private static void renderParticle(DrawContext context, GlintSparkleParticle p) {
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