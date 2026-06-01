package dev.codexbat.vesper.client.glint.internal;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class GlintTextures {
    public static final Identifier WHITE_PIXEL = Identifier.of("vesper", "textures/misc/white_pixel");

    private static volatile boolean initialized = false;

    /** Call this once from the render thread before using WHITE_PIXEL. */
    public static void ensureInitialized() {
        if (initialized) return;

        NativeImage image = new NativeImage(NativeImage.Format.RGBA, 1, 1, false);
        image.setColorArgb(0, 0, 0xFFFFFFFF);

        NativeImageBackedTexture texture = new NativeImageBackedTexture(
                () -> "vesper_white_pixel",   // GPU debug label
                image
        );

        MinecraftClient.getInstance()
                .getTextureManager()
                .registerTexture(WHITE_PIXEL, texture);

        initialized = true;
    }
}