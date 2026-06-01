package dev.codexbat.vesper.client.glint.internal.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

/**
 * Registered with Fabric's ParticleFactoryRegistry solely to obtain and
 * cache the atlas sprite for the WORLD_GLINT particle type.
 *
 * createParticle() is never called at runtime. WorldGlintParticleManager
 * creates particles directly via ParticleManager.addParticle(Particle)
 * so that entity references can be passed to the constructor — something
 * the effect/factory pipeline cannot carry.
 */
@Environment(EnvType.CLIENT)
public final class WorldGlintParticleFactory
        implements ParticleFactory<WorldGlintParticleEffect> {

    public WorldGlintParticleFactory(SpriteProvider spriteProvider) {
        // Do NOT call getSprite() here — the atlas isn't stitched yet.
        // Store the provider; sprites are resolved at spawn time.
        WorldGlintParticleManager.SPRITE_PROVIDER = spriteProvider;
    }

    @Override
    public Particle createParticle(WorldGlintParticleEffect effect,
                                   ClientWorld world,
                                   double x, double y, double z,
                                   double velX, double velY, double velZ,
                                   Random random) {
        // Never called — particles are created directly in WorldGlintParticleManager.
        // Returning null is safe; ParticleManager silently drops null results.
        return null;
    }
}