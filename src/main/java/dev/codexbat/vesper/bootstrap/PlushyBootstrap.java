package dev.codexbat.vesper.bootstrap;

import dev.codexbat.vesper.Vesper;
import dev.codexbat.vesper.plush.PlushyDefinition;
import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.plush.loot.PlushyDropLootFunction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class PlushyBootstrap {
    private PlushyBootstrap() {}

    public static void initialize() {
        // PlushyRegistry - check that to see what this is all about
        PlushyRegistry.initialize();

        // Register plushy definitions – they automatically get an Item and become usable
        PlushyRegistry.register(
                PlushyDefinition.builder(
                                Vesper.id("codex_plush"),
                                Vesper.id("textures/entity/plush/codex.png")
                        )
                        .helmet()     // allowed slots (true)
                        .mainhand()
                        .offhand()
                        .build()
        );

        Registry.register(
                Registries.LOOT_FUNCTION_TYPE,
                Vesper.id("plushy_drop"),
                PlushyDropLootFunction.TYPE
        );
    }
}
