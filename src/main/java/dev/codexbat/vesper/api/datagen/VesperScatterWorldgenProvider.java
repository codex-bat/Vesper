package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**

 Worldgen datagen base for scatter definitions.
 Subclasses should turn ScatterWorldgenSpec objects into the actual

 configured/placed feature registrations used by the mod.
 */
public abstract class VesperScatterWorldgenProvider extends FabricDynamicRegistryProvider {

    protected VesperScatterWorldgenProvider(FabricDataOutput output,
                                            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected final void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        generateScatterWorldgen(registries, entries);
    }

    protected abstract void generateScatterWorldgen(RegistryWrapper.WrapperLookup registries, Entries entries);

    @Override
    public abstract String getName();
}