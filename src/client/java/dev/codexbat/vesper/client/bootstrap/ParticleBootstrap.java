package dev.codexbat.vesper.client.bootstrap;

import dev.codexbat.vesper.client.particle.VesperParticleTypes;

public class ParticleBootstrap {
    private ParticleBootstrap() {}

    public static void initialize() {
        // Le Particle
        VesperParticleTypes.register();
    }
}
