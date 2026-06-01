package dev.codexbat.vesper.bootstrap;

import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.registry.ModItemGroups;

public final class VesperBootstrap {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        // I could just do... PlushyRegistry.initialize();
        // but nah, it's cooler this way!
        PlushyBootstrap.initialize();
        ModItemGroups.initialize();
    }
}