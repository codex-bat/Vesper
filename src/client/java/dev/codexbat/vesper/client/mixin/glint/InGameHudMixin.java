package dev.codexbat.vesper.client.mixin.glint;

import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import dev.codexbat.vesper.client.glint.internal.render.AmbientParticleManager;
import dev.codexbat.vesper.client.glint.internal.render.SparkleParticleManager;
import dev.codexbat.vesper.client.glint.internal.render.TrailParticleManager;
import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Hooks {@link InGameHud} to drive ambient and sparkle particle effects for
 * hotbar items while the player is in the world.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li><strong>Spawn</strong> — each frame, for every hotbar slot holding an
 *       {@link AmbientGlint} or {@link SparkleGlint} item, particles are spawned
 *       at that slot's screen-space centre. This runs unconditionally so hotbar
 *       sparkles are visible even when an inventory screen is open on top.</li>
 *   <li><strong>Tick + Render</strong> — advances physics and draws particles,
 *       but <em>only when no screen is currently open</em>. When an inventory
 *       screen is open, {@code HandledScreenMixin} owns the tick/render cycle,
 *       preventing the managers from being advanced twice per frame.</li>
 * </ul>
 *
 * <h2>Hotbar slot geometry:</h2>
 * <p>Vanilla draws the hotbar background at
 * {@code x = scaledWidth/2 - 91, y = scaledHeight - 22}.
 * Each of the 9 slots is 20 px wide; item icons have a 3 px inset from the
 * slot edge, giving an icon left edge at {@code slotX + 3} and a centre at
 * {@code slotX + 3 + 8 = slotX + 11}, i.e.
 * {@code scaledWidth/2 - 91 + slot*20 + 11 = scaledWidth/2 - 80 + slot*20}.
 * Vertically, the 16 px icon sits 3 px from the top of the 22 px bar, so the
 * centre is at {@code scaledHeight - 22 + 3 + 8 = scaledHeight - 11}.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    /**
     * Runs at the tail of every HUD render.
     *
     * <p>Particle spawning always occurs for hotbar slots.
     * Ticking and rendering only occur when no screen is open —
     * if a screen is open, {@code HandledScreenMixin} handles those steps.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void vesper$renderHotbarParticles(
            DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

        if (client.player == null) return;

        int screenWidth  = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // HandledScreen (inventory, chest, etc.) → HandledScreenMixin owns tick/render.
        // Any other screen (chat, social interactions, etc.) → we still own tick/render,
        // but dim the hotbar particles since the UI overlaps that area.
        boolean handledOpen = client.currentScreen instanceof HandledScreen;
        boolean otherOpen   = client.currentScreen != null && !handledOpen;

        float hotbarAlpha = handledOpen ? 0.3f
                : otherOpen   ? 0.6f
                :               1.0f;

        PlayerInventory inventory = client.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            Optional<GlintDefinition> glint = VesperGlintRegistry.getGlint(stack.getItem());
            if (glint.isEmpty()) continue;

            double slotCx = screenWidth  / 2.0 - 80 + i * 20;
            double slotCy = screenHeight - 11.0;

            GlintDefinition def = glint.get();
            if (def instanceof AmbientGlint ambient) {
                AmbientParticleManager.spawn(slotCx, slotCy, ambient.getConfig(), hotbarAlpha);
            } else if (def instanceof SparkleGlint sparkle) {
                SparkleParticleManager.spawn(slotCx, slotCy, sparkle.getIdleConfig(), hotbarAlpha);
            }
        }

        // Skip if a HandledScreen is open — it owns the tick/render cycle.
        if (!handledOpen) {
            TrailParticleManager.tick();
            TrailParticleManager.render(context);

            AmbientParticleManager.tick();
            AmbientParticleManager.render(context);

            SparkleParticleManager.tick();
            SparkleParticleManager.render(context);
        }
    }
}