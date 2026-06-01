package dev.codexbat.vesper.client.bootstrap;

import static dev.codexbat.vesper.client.bootstrap.BuiltinGlints.registerBuiltinGlints;

public final class ClientBootstrap {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        // THIS NEEDS TO BE BEFORE THE BUILTIN GLINTS, BECAUSE OF registerFactory();
        // WELL OF COURSE, NEVER MIND!!!
        // HOW TF WAS I EXPECTING TO USE PARTICLES THAT I HAVEN'T EVEN YET REGISTERED... LIKE WTF!?!?!
        // I'M SO GODDAMN STUPID SOMETIMES, I THOUGHT "oh, glintbootstrap is supposed to be before BuiltinGlints" fucking moron.
        ParticleBootstrap.initialize();
        registerBuiltinGlints();
        GlintBootstrap.initialize();
        PlushyClientBootstrap.initialize();
    }
}