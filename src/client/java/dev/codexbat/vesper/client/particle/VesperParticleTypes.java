package dev.codexbat.vesper.client.particle;

import com.mojang.serialization.MapCodec;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleManager;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleEffect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers Vesper's custom {@link ParticleType}s.
 *
 * <p>Calling {@link #register()} once from {@link ClientModInitializer}
 * <em>before</em> {@link WorldGlintParticleManager}
 * is used.
 *
 * <pre>{@code
 * @Override public void onInitializeClient() {
 *     VesperParticleTypes.register();
 *     WorldGlintParticleManager.registerFactory();
 * }
 * }</pre>
 *
 * <p>{@link net.minecraft.particle.ParticleType} is abstract in 1.21.11, so we
 * register an anonymous subclass that returns stub codecs.  The codecs are never
 * called because all particles are spawned client-side via
 * {@code ParticleManager.addParticle()} and are never sent over the network.
 */
@Environment(EnvType.CLIENT)
public final class VesperParticleTypes {

    /** The single particle type used for all world-space glint particles. */
    public static ParticleType<WorldGlintParticleEffect> WORLD_GLINT;

    public static void register() {
        WORLD_GLINT = Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of("vesper", "world_glint"),
                new ParticleType<WorldGlintParticleEffect>(false) {

                    @Override
                    public MapCodec<WorldGlintParticleEffect> getCodec() {
                        return WorldGlintParticleEffect.STUB_CODEC;
                    }

                    @Override
                    public PacketCodec<? super RegistryByteBuf, WorldGlintParticleEffect> getPacketCodec() {
                        return WorldGlintParticleEffect.STUB_PACKET_CODEC;
                    }
                }
        );
    }

    private VesperParticleTypes() {}
}