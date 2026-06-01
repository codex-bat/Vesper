package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public abstract class VesperRecipeProvider extends RecipeGenerator.RecipeProvider {

    protected VesperRecipeProvider(FabricDataOutput output,
                                   CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected final RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries,
                                                       RecipeExporter exporter) {
        return new RecipeGenerator(registries, exporter) {
            @Override
            public void generate() {
                buildRecipes(new VesperRecipes(this, exporter, registries));
            }
        };
    }

    protected abstract void buildRecipes(VesperRecipes recipes);
}