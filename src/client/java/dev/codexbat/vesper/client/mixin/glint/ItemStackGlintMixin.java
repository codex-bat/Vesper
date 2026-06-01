package dev.codexbat.vesper.client.mixin.glint;

import dev.codexbat.vesper.api.glint.GlintCategory;
import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Hooks {@link ItemStack#hasGlint()} so that items registered with a
 * {@link GlintCategory#STANDARD} or {@link GlintCategory#SPECIAL} glint are
 * recognized by Minecraft's rendering pipeline as having a glint.
 *
 * <p>{@link GlintCategory#TRAIL} items are intentionally excluded here — their
 * visual effect is rendered separately in screen space and must not trigger the
 * 3D model glint pass.
 *
 * <p>This mixin is <em>additive</em>: if the item would already return {@code true}
 * (e.g. it's enchanted), we do not change that. We only force {@code true} when
 * Vesper has a relevant definition and the item wouldn't glow otherwise.
 */
@Mixin(ItemStack.class)
public class ItemStackGlintMixin {

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void vesper$injectCustomGlint(CallbackInfoReturnable<Boolean> cir) {
        // If vanilla already says true, nothing to do
        // (we only inject via cancellable to force true, not to suppress)
        ItemStack self = (ItemStack)(Object)this;
        Optional<GlintDefinition> glint = VesperGlintRegistry.getGlint(self.getItem());

        if (glint.isPresent()) {
            GlintCategory cat = glint.get().category();
            if (cat == GlintCategory.STANDARD || cat == GlintCategory.SPECIAL) {
                cir.setReturnValue(true);
            }
            // TRAIL: do nothing — handled by HandledScreenMixin
        }
    }
}