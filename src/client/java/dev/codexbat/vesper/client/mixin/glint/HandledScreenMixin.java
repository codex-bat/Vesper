package dev.codexbat.vesper.client.mixin.glint;

import dev.codexbat.vesper.api.glint.*;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.type.TrailGlint;
import dev.codexbat.vesper.client.glint.internal.render.AmbientParticleManager;
import dev.codexbat.vesper.client.glint.internal.render.SparkleParticleManager;
import dev.codexbat.vesper.client.glint.internal.render.TrailParticleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Hooks {@link HandledScreen} to drive the full lifecycle of
 * {@link GlintCategory#TRAIL},
 * {@link GlintCategory#AMBIENT}, and
 * {@link GlintCategory#SPARKLE} particles
 * while any inventory screen is open.
 *
 * <h2>Per-glint type behaviour:</h2>
 * <ul>
 *   <li><strong>TRAIL</strong> — particles spawned at cursor position each frame;
 *       only active when the cursor holds a trail-glint item.</li>
 *   <li><strong>AMBIENT</strong> — idle particles spawned at every visible slot that
 *       holds an ambient-glint item; additionally, cursor-held ambient items emit
 *       particles at the cursor position using the same single config.</li>
 *   <li><strong>SPARKLE</strong> — idle particles spawned at slots using
 *       {@link SparkleGlint#getIdleConfig()}; cursor-held sparkle items emit
 *       using {@link SparkleGlint#getTrailConfig()} (which may equal the idle
 *       config when {@link SparkleGlint#isSharedParticle()} is {@code true}).</li>
 * </ul>
 *
 * <p>All three managers are ticked and rendered once per frame here.
 * {@code InGameHudMixin} skips ticking/rendering while any screen is open to
 * prevent double-ticking.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    // Screen-space top-left corner, used to convert slot-local to screen coords.
    @Shadow @Final protected int x;
    @Shadow @Final protected int y;

    /**
     * Injected at the very end of each frame's render pass.
     *
     * <p><strong>Cursor offset note:</strong> vanilla draws the cursor-held item at
     * {@code (mouseX - 8, mouseY - 8)}, so the item centre is exactly
     * {@code (mouseX, mouseY)} — no offset needed when spawning at the cursor.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void vesper$renderParticles(
            DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        HandledScreen<T> self    = (HandledScreen<T>)(Object)this;
        ScreenHandler    handler = self.getScreenHandler();

        // --- SLOT PARTICLES: ambient + sparkle idle for every visible slot -----
        for (Slot slot : handler.slots) {
            ItemStack slotStack = slot.getStack();
            if (slotStack.isEmpty()) continue;

            Optional<GlintDefinition> glint = VesperGlintRegistry.getGlint(slotStack.getItem());
            if (glint.isEmpty()) continue;

            // Slot coords are relative to the screen's top-left corner; +8 centres on the icon.
            double slotCx = this.x + slot.x + 8;
            double slotCy = this.y + slot.y + 8;

            GlintDefinition def = glint.get();
            if (def instanceof AmbientGlint ambient) {
                AmbientParticleManager.spawn(slotCx, slotCy, ambient.getConfig());
            } else if (def instanceof SparkleGlint sparkle) {
                SparkleParticleManager.spawn(slotCx, slotCy, sparkle.getIdleConfig());
            }
            // TrailGlint has no idle slot emission — it only reacts to cursor motion.
        }

        // --- CURSOR-HELD ITEM: trail / ambient / sparkle trail ----------------
        ItemStack cursorStack = handler.getCursorStack();
        if (!cursorStack.isEmpty()) {
            Optional<GlintDefinition> glint = VesperGlintRegistry.getGlint(cursorStack.getItem());
            if (glint.isPresent()) {
                GlintDefinition def = glint.get();

                if (def instanceof TrailGlint trailGlint) {
                    // Pure trail glint: spawn at cursor centre (see offset note above).
                    TrailParticleManager.spawn(mouseX, mouseY, trailGlint);

                } else if (def instanceof AmbientGlint ambient) {
                    // Ambient glint held by cursor: same single config for cursor emission.
                    AmbientParticleManager.spawn(mouseX, mouseY, ambient.getConfig());

                } else if (def instanceof SparkleGlint sparkle) {
                    // Sparkle glint held by cursor: use trail config (may equal idle config).
                    SparkleParticleManager.spawn(mouseX, mouseY, sparkle.getTrailConfig());
                }
            }
        }

        // --- TICK + RENDER (all three managers, once per frame) ---------------
        TrailParticleManager.tick();
        TrailParticleManager.render(context);

        AmbientParticleManager.tick();
        AmbientParticleManager.render(context);

        SparkleParticleManager.tick();
        SparkleParticleManager.render(context);
    }

    /** Clears all active particles from every manager when the screen is closed. */
    @Inject(method = "removed", at = @At("HEAD"))
    private void vesper$clearParticlesOnClose(CallbackInfo ci) {
        TrailParticleManager.clear();
        AmbientParticleManager.clear();
        SparkleParticleManager.clear();
    }
}