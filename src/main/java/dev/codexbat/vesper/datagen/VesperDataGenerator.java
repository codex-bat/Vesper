package dev.codexbat.vesper.datagen;

import dev.codexbat.vesper.api.datagen.VesperDatagenPlugin;
import dev.codexbat.vesper.bootstrap.VesperBootstrap;
import dev.codexbat.vesper.datagen.provider.ModBlockLootProvider;
import dev.codexbat.vesper.datagen.provider.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;

public final class VesperDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        //VesperBootstrap.initialize();

        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModBlockLootProvider::new);
        pack.addProvider(ModRecipeProvider::new);

        FabricLoader.getInstance()
                .getEntrypointContainers(
                        "vesper-datagen",
                        VesperDatagenPlugin.class
                )
                .forEach(container ->
                        container.getEntrypoint().buildDatagen(pack)
                );
    }
}