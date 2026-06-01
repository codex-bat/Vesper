package dev.codexbat.vesper.datagen.provider;

import dev.codexbat.vesper.Vesper;
import dev.codexbat.vesper.api.datagen.VesperRecipeProvider;
import dev.codexbat.vesper.api.datagen.VesperRecipes;
import dev.codexbat.vesper.api.recipe.ComponentIngredient;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates crafting recipes for Vesper's built-in content.
 */
@SuppressWarnings("unused")
public final class ModRecipeProvider extends VesperRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output,
                             CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void buildRecipes(VesperRecipes recipes) {
        addCodexPlushRecipe(recipes);
    }

    private void addCodexPlushRecipe(VesperRecipes recipes) {
        var patternLookup = recipes.registries().getOrThrow(RegistryKeys.BANNER_PATTERN);

        ItemStack banner = new ItemStack(Items.BLACK_BANNER);
        banner.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(List.of(
                new BannerPatternsComponent.Layer(patternLookup.getOrThrow(BannerPatterns.SKULL),         DyeColor.GRAY),
                new BannerPatternsComponent.Layer(patternLookup.getOrThrow(BannerPatterns.RHOMBUS),       DyeColor.BLACK),
                new BannerPatternsComponent.Layer(patternLookup.getOrThrow(BannerPatterns.STRIPE_MIDDLE), DyeColor.BLACK),
                new BannerPatternsComponent.Layer(patternLookup.getOrThrow(BannerPatterns.PIGLIN),        DyeColor.GRAY)
        )));

        recipes.shapeless(PlushyRegistry.getItem(Vesper.id("codex_plush")))
                .input(Items.BLACK_WOOL)
                .input(ComponentIngredient.of(banner))
                .criterion("has_banner", recipes.conditionsFromItem(Items.BLACK_BANNER))
                .save(Vesper.id("codex_plush"));
    }

    @Override
    public String getName() {
        return "Vesper Recipe Provider";
    }
}