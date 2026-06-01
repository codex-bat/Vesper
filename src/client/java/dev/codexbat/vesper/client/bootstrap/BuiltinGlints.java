package dev.codexbat.vesper.client.bootstrap;

import dev.codexbat.vesper.api.glint.ParticleConfig;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.type.SpecialGlint;
import dev.codexbat.vesper.api.glint.type.StandardGlint;
import net.minecraft.item.Items;

public class BuiltinGlints {
    /**
     * Built-in glint registrations.
     * Basically tests during development~
     */
    static void registerBuiltinGlints() {

        // Sparkle
        VesperGlintRegistry.register(Items.SNOWBALL, SparkleGlint.builder()
                .worldParticles(true)
                .idle(ParticleConfig.builder()
                        .color(0xAAFFFFFF).spawnRate(1).lifetime(35).spread(6.0f)
                        .gravity(0.02f).sizeMin(1.0f).sizeMax(2.0f)
                        .shape(ParticleConfig.Shape.SQUARE).fadeOut(true).build())
                .sharedParticle(false)
                .trail(ParticleConfig.builder()
                        .color(0xCCFFFFFF).spawnRate(4).lifetime(22).spread(5.0f)
                        .gravity(0.04f).sizeMin(1.5f).sizeMax(3.5f)
                        .shape(ParticleConfig.Shape.SQUARE).fadeOut(true).build())
                .build());

        // Ambient
        VesperGlintRegistry.register(Items.AMETHYST_SHARD, AmbientGlint.builder()
                .worldParticles(true)
                .config(ParticleConfig.builder()
                        .color(0xBBCC88FF)
                        .spawnRate(1).lifetime(40).spread(7.0f)
                        .shape(ParticleConfig.Shape.DIAMOND).fadeOut(true).build())
                .build());

        // Standard
        VesperGlintRegistry.register(Items.NETHERITE_SWORD, StandardGlint.builder()
                .color(0xFFFF4400)
                .speed(1.5f)
                .intensity(0.8f)
                .build());

        // Special
        VesperGlintRegistry.register(Items.TOTEM_OF_UNDYING, SpecialGlint.builder()
                .effectType(SpecialGlint.EffectType.PULSE)
                .primaryColor(0xFFFFD700)
                .secondaryColor(0xFFFF6600)
                .speed(2.0f)
                .build());
    }
}
