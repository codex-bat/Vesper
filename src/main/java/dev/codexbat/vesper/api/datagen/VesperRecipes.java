package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.tag.TagKey;
import org.jspecify.annotations.Nullable;

public final class VesperRecipes {

    private final RecipeGenerator generator;
    private final RecipeExporter exporter;
    private final RegistryWrapper.WrapperLookup registries;

    VesperRecipes(RecipeGenerator generator,
                  RecipeExporter exporter,
                  RegistryWrapper.WrapperLookup registries) {
        this.generator = generator;
        this.exporter = exporter;
        this.registries = registries;
    }

    public RegistryWrapper.WrapperLookup registries() {
        return registries;
    }

    public RegistryEntryLookup<Item> items() {
        return registries.getOrThrow(RegistryKeys.ITEM);
    }

    public RegistryKey<Recipe<?>> recipeKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.RECIPE, id);
    }

    public AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(ItemConvertible item) {
        return generator.conditionsFromItem(item);
    }

    public AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(NumberRange.IntRange count,
                                                                                         ItemConvertible item) {
        return generator.conditionsFromItem(count, item);
    }

    public AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromTag(TagKey<Item> tag) {
        return generator.conditionsFromTag(tag);
    }

    public Ingredient ingredientFromTag(TagKey<Item> tag) {
        return generator.ingredientFromTag(tag);
    }

    public ShapelessBuilder shapeless(ItemConvertible output) {
        return new ShapelessBuilder(generator.createShapeless(RecipeCategory.MISC, output), exporter);
    }

    public ShapelessBuilder shapeless(ItemConvertible output, int count) {
        return new ShapelessBuilder(generator.createShapeless(RecipeCategory.MISC, output, count), exporter);
    }

    public ShapelessBuilder shapeless(RecipeCategory category, ItemConvertible output) {
        return new ShapelessBuilder(generator.createShapeless(category, output), exporter);
    }

    public ShapelessBuilder shapeless(RecipeCategory category, ItemConvertible output, int count) {
        return new ShapelessBuilder(generator.createShapeless(category, output, count), exporter);
    }

    public ShapelessBuilder shapeless(RecipeCategory category, ItemStack output) {
        return new ShapelessBuilder(generator.createShapeless(category, output), exporter);
    }

    public ShapedBuilder shaped(ItemConvertible output) {
        return new ShapedBuilder(generator.createShaped(RecipeCategory.MISC, output), exporter);
    }

    public ShapedBuilder shaped(ItemConvertible output, int count) {
        return new ShapedBuilder(generator.createShaped(RecipeCategory.MISC, output, count), exporter);
    }

    public ShapedBuilder shaped(RecipeCategory category, ItemConvertible output) {
        return new ShapedBuilder(generator.createShaped(category, output), exporter);
    }

    public ShapedBuilder shaped(RecipeCategory category, ItemConvertible output, int count) {
        return new ShapedBuilder(generator.createShaped(category, output, count), exporter);
    }

    public static final class ShapelessBuilder {
        private final ShapelessRecipeJsonBuilder delegate;
        private final RecipeExporter exporter;

        private ShapelessBuilder(ShapelessRecipeJsonBuilder delegate, RecipeExporter exporter) {
            this.delegate = delegate;
            this.exporter = exporter;
        }

        public ShapelessBuilder input(ItemConvertible item) {
            delegate.input(item);
            return this;
        }

        public ShapelessBuilder input(ItemConvertible item, int amount) {
            delegate.input(item, amount);
            return this;
        }

        public ShapelessBuilder input(Ingredient ingredient) {
            delegate.input(ingredient);
            return this;
        }

        public ShapelessBuilder input(Ingredient ingredient, int amount) {
            delegate.input(ingredient, amount);
            return this;
        }

        public ShapelessBuilder input(CustomIngredient ingredient) {
            delegate.input(ingredient.toVanilla());
            return this;
        }

        public ShapelessBuilder input(CustomIngredient ingredient, int amount) {
            delegate.input(ingredient.toVanilla(), amount);
            return this;
        }

        public ShapelessBuilder criterion(String name, AdvancementCriterion<?> criterion) {
            delegate.criterion(name, criterion);
            return this;
        }

        public ShapelessBuilder group(@Nullable String group) {
            delegate.group(group);
            return this;
        }

        public void save(Identifier id) {
            delegate.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, id));
        }

        public void save(RegistryKey<Recipe<?>> key) {
            delegate.offerTo(exporter, key);
        }
    }

    public static final class ShapedBuilder {
        private final ShapedRecipeJsonBuilder delegate;
        private final RecipeExporter exporter;

        private ShapedBuilder(ShapedRecipeJsonBuilder delegate, RecipeExporter exporter) {
            this.delegate = delegate;
            this.exporter = exporter;
        }

        public ShapedBuilder input(char symbol, ItemConvertible item) {
            delegate.input(symbol, item);
            return this;
        }

        public ShapedBuilder input(char symbol, Ingredient ingredient) {
            delegate.input(symbol, ingredient);
            return this;
        }

        public ShapedBuilder input(char symbol, CustomIngredient ingredient) {
            delegate.input(symbol, ingredient.toVanilla());
            return this;
        }

        public ShapedBuilder input(char symbol, TagKey<Item> tag) {
            delegate.input(symbol, tag);
            return this;
        }

        public ShapedBuilder pattern(String pattern) {
            delegate.pattern(pattern);
            return this;
        }

        public ShapedBuilder criterion(String name, AdvancementCriterion<?> criterion) {
            delegate.criterion(name, criterion);
            return this;
        }

        public ShapedBuilder group(@Nullable String group) {
            delegate.group(group);
            return this;
        }

        public ShapedBuilder showNotification(boolean showNotification) {
            delegate.showNotification(showNotification);
            return this;
        }

        public void save(Identifier id) {
            delegate.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, id));
        }

        public void save(RegistryKey<Recipe<?>> key) {
            delegate.offerTo(exporter, key);
        }
    }
}