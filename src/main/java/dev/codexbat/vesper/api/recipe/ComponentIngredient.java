package dev.codexbat.vesper.api.recipe;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CustomIngredient} that matches stacks by item type
 * <em>and</em> full component equality.
 *
 * <p>An incoming stack matches iff {@link ItemStack#areItemsAndComponentsEqual}
 * returns {@code true} when compared to the template. This means:
 * <ul>
 *   <li>same item type, and</li>
 *   <li>identical component map - extra components (a renamed banner,
 *       a differently-ordered pattern, etc.) will <b>not</b> match.</li>
 * </ul>
 *
 * <p><b>Registration</b> - once in mod initializer, before recipes load:
 * <pre>
 * CustomIngredientSerializer.register(ComponentIngredient.SERIALIZER);
 * </pre>
 *
 * <p><b>Datagen usage:</b>
 * <pre>
 * ItemStack banner = new ItemStack(Items.BLACK_BANNER);
 * banner.set(DataComponentTypes.BANNER_PATTERNS, myPatterns);
 *
 * ShapelessRecipeJsonBuilder.create(...)
 *     .input(ComponentIngredient.of(banner).toVanilla())
 *     ...
 * </pre>
 *
 * <p><b>Generated JSON shape:</b>
 * <pre>
 * {
 *   "fabric:type": "vesper:component",
 *   "item": {
 *     "id":         "minecraft:black_banner",
 *     "components": { "minecraft:banner_patterns": [...] }
 *   }
 * }
 * </pre>
 */
public final class ComponentIngredient implements CustomIngredient {

    /** Serializer identifier - matches the {@code "fabric:type"} field in JSON. */
    public static final Identifier ID = Identifier.of("vesper", "component");

    /**
     * Passing it to {@link CustomIngredientSerializer#register} in mod initializer
     * so Minecraft can deserialize the ingredient JSON on startup.
     */
    public static final Serializer SERIALIZER = new Serializer();

    // Internal state

    /** Captured at build time; never modified after construction. */
    private final ItemStack template;

    private ComponentIngredient(ItemStack template) {
        this.template = template.copy();
    }

    // Factory
    /**
     * Create a {@code ComponentIngredient} from any stack.
     * The ingredient will require an exact match on item type and all
     * components via {@link ItemStack#areItemsAndComponentsEqual}.
     */
    public static ComponentIngredient of(ItemStack template) {
        return new ComponentIngredient(template);
    }

    // CustomIngredient

    /**
     * {@code true} only when the stack is component-equal to the template.
     */
    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return ItemStack.areItemsAndComponentsEqual(template, stack);
    }

    /**
     * Returns the item registry entry for the template stack.
     * This lets recipe viewers display a representative item.
     */
    @Override
    public Stream<RegistryEntry<Item>> getMatchingItems() {
        if (template.isEmpty()) return Stream.empty();
        return Stream.of(template.getRegistryEntry());
    }

    /**
     * Returns the template stack for display in recipe viewers (JEI/REI).
     * A defensive copy is returned to prevent external mutation.
     */
    public List<ItemStack> getMatchingStacks() {
        return List.of(template.copy());
    }

    /**
     * Always {@code true} - component equality requires per-stack evaluation
     * and cannot be short-circuited by item-type alone.
     */
    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    // Serializer

    public static final class Serializer implements CustomIngredientSerializer<ComponentIngredient> {

        /**
         * Delegates entirely to {@link ItemStack#CODEC}, which already handles
         * item ID + count + component map. The {@code "item"} field name keeps
         * the JSON readable and consistent with vanilla's item-stack format.
         */
        private static final MapCodec<ComponentIngredient> CODEC =
                ItemStack.CODEC
                        .xmap(ComponentIngredient::new, ci -> ci.template)
                        .fieldOf("item");

        /** Mirrors {@link #CODEC} over the packet channel. */
        private static final PacketCodec<RegistryByteBuf, ComponentIngredient> PACKET_CODEC =
                ItemStack.PACKET_CODEC.xmap(ComponentIngredient::new, ci -> ci.template);

        private Serializer() {}

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        // THIS IS THE CORRECT FORM
        @Override
        public MapCodec<ComponentIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ComponentIngredient> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}