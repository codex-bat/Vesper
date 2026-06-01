package dev.codexbat.vesper.client.glint.internal.render;

import com.mojang.serialization.MapCodec;
import dev.codexbat.vesper.api.glint.ParticleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

/**
 * Carries a {@link ParticleConfig} reference through the client-side particle
 * spawn path.
 *
 * <p><strong>Client-only, never serialized.</strong> All particles are spawned
 * directly by {@link WorldGlintParticleManager} on the client, so the codecs
 * exposed by {@link #STUB_CODEC} and {@link #STUB_PACKET_CODEC} are stubs that
 * are never invoked.  They are {@code public} so that
 * {@link dev.codexbat.vesper.client.particle.VesperParticleTypes} can supply them
 * to the anonymous {@link ParticleType} subclass it registers.
 */
@Environment(EnvType.CLIENT)
public final class WorldGlintParticleEffect implements ParticleEffect {

    // Public so VesperParticleTypes can pass them into its anonymous ParticleType.
    public static final MapCodec<WorldGlintParticleEffect> STUB_CODEC =
            MapCodec.unit(() -> {
                throw new UnsupportedOperationException(
                        "WorldGlintParticleEffect is client-only and must not be decoded.");
            });

    public static final PacketCodec<RegistryByteBuf, WorldGlintParticleEffect> STUB_PACKET_CODEC =
            PacketCodec.unit(new WorldGlintParticleEffect(ParticleConfig.builder().build(), 1.0f));

    private final ParticleConfig config;
    private final float alphaMultiplier;

    public WorldGlintParticleEffect(ParticleConfig config, float alphaMultiplier) {
        this.config          = config;
        this.alphaMultiplier = alphaMultiplier;
    }

    public ParticleConfig getConfig()         { return config; }
    public float         getAlphaMultiplier() { return alphaMultiplier; }

    @Override
    public ParticleType<?> getType() {
        return dev.codexbat.vesper.client.particle.VesperParticleTypes.WORLD_GLINT;
    }
}