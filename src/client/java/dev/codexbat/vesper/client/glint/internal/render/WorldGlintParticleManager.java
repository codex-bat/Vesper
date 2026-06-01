package dev.codexbat.vesper.client.glint.internal.render;

import dev.codexbat.vesper.api.glint.ParticleConfig;
import dev.codexbat.vesper.client.particle.VesperParticleTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;

import java.util.Random;

/**
 * Spawns entity-tracking world-space glint particles directly via
 * ParticleManager.addParticle(Particle), bypassing the effect/factory
 * pipeline so that entity references can be passed to the particle.
 *
 * The factory is still registered to obtain the sprite from the particle
 * atlas at startup; it is never used for actual particle creation.
 *
 * Spread jitter is applied in entity-relative space: the jitter offsets are
 * baked into the particle's fixed offset, so the scatter cloud translates
 * with the entity rather than staying at the world-space spawn point.
 */
@Environment(EnvType.CLIENT)
public final class WorldGlintParticleManager {

    private static final float WORLD_PIXEL = 0.016f;

    /** Converts screen-space frame count (60 fps) to game tick count (20 tps). */
    public static final float LIFETIME_SCALE = 20.0f / 60.0f;

    /**
     * Cached from WorldGlintParticleFactory at registration time.
     * Null until registerFactory() has been called and the factory constructed.
     */
    static Sprite SPRITE;

    private static final Random RANDOM = new Random();

    private WorldGlintParticleManager() {}

    public static void registerFactory() {
        ParticleFactoryRegistry.getInstance()
                .register(VesperParticleTypes.WORLD_GLINT, WorldGlintParticleFactory::new);
    }

    /**
     * Spawns glint particles tracking the given entity.
     *
     * @param entity  entity whose movement the particles track
     * @param offsetX X from entity feet in world-axis space (baked at spawn time)
     * @param offsetY Y from entity feet in world-axis space
     * @param offsetZ Z from entity feet in world-axis space
     */
    public static void spawn(Entity entity,
                             double offsetX, double offsetY, double offsetZ,
                             ParticleConfig config) {
        spawn(entity, offsetX, offsetY, offsetZ, config, 1.0f);
    }

    /** Cached from WorldGlintParticleFactory at registration time. */
    static SpriteProvider SPRITE_PROVIDER;   // was: static Sprite SPRITE

    // spawn() — guard against unregistered provider
    public static void spawn(Entity entity,
                             double offsetX, double offsetY, double offsetZ,
                             ParticleConfig config, float alphaMultiplier) {
        if (SPRITE_PROVIDER == null) return;  // was: SPRITE == null
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.particleManager == null) return;

        for (int i = 0; i < config.getSpawnRate(); i++) {
            spawnOne(client, entity, offsetX, offsetY, offsetZ, config, alphaMultiplier);
        }
    }

    private static void spawnOne(MinecraftClient client,
                                 Entity entity,
                                 double offsetX, double offsetY, double offsetZ,
                                 ParticleConfig config, float alphaMultiplier) {
        if (SPRITE_PROVIDER == null) return;

        float spread = config.getSpread() * WORLD_PIXEL;
        double ox = offsetX + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;
        double oy = offsetY + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;
        double oz = offsetZ + (RANDOM.nextFloat() - 0.5f) * spread * 2.0;

        // Resolve the sprite safely – if the atlas isn’t ready, just skip this particle.
        Sprite sprite;
        try {
            sprite = SPRITE_PROVIDER.getSprite(0, 1);
        } catch (NullPointerException e) {
            return;   // atlas not yet stitched
        }
        if (sprite == null) return;

        client.particleManager.addParticle(
                new WorldGlintParticle(client.world, entity, ox, oy, oz,
                        sprite, config, alphaMultiplier));
    }
}