package dev.codexbat.vesper.api.glint;

import dev.codexbat.vesper.Vesper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;

import java.util.*;

/**
 * Central registry mapping {@link Item}s to their {@link GlintDefinition}.
 *
 * <h2>Usage (from client entrypoint):</h2>
 * <pre>{@code
 * VesperGlintRegistry.register(Items.DIAMOND_SWORD,
 *     StandardGlint.builder().color(0xFF00FFFF).build()
 * );
 * }</pre>
 *
 * <p>Registration should happen during {@code onInitializeClient()} before the
 * first world render tick. Registrations are mutable; calling {@link #register}
 * again on the same item overwrites the previous entry.
 */
@Environment(EnvType.CLIENT)
public final class VesperGlintRegistry {

    private static final Map<Item, GlintDefinition> REGISTRY = new LinkedHashMap<>();

    private VesperGlintRegistry() {}

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a custom glint for the given item.
     * Overwrites any previously registered glint for that item.
     *
     * @param item       The item that should carry this glint.
     * @param definition The glint definition to apply.
     * @throws IllegalArgumentException if {@code item} or {@code definition} is null.
     */
    public static void register(Item item, GlintDefinition definition) {
        Objects.requireNonNull(item,       "item must not be null");
        Objects.requireNonNull(definition, "definition must not be null");
        if (REGISTRY.containsKey(item)) {
            Vesper.LOGGER.warn("[Vesper] GlintRegistry: overwriting existing glint for {}", item);
        }
        REGISTRY.put(item, definition);
        Vesper.LOGGER.debug("[Vesper] GlintRegistry: registered {} glint for {}",
                definition.category().name().toLowerCase(), item);
    }

    /**
     * Removes the glint registration for the given item, if any.
     *
     * @param item The item whose glint should be removed.
     * @return {@code true} if a registration was removed.
     */
    public static boolean unregister(Item item) {
        return REGISTRY.remove(item) != null;
    }

    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link GlintDefinition} for the given item, if one is registered.
     *
     * @param item The item to look up.
     * @return An {@link Optional} containing the definition, or {@link Optional#empty()}.
     */
    public static Optional<GlintDefinition> getGlint(Item item) {
        return Optional.ofNullable(REGISTRY.get(item));
    }

    /**
     * @param item The item to check.
     * @return {@code true} if any glint (of any category) is registered for this item.
     */
    public static boolean hasGlint(Item item) {
        return REGISTRY.containsKey(item);
    }

    /**
     * @param item     The item to check.
     * @param category The category to match.
     * @return {@code true} if a glint of the specified category is registered for this item.
     */
    public static boolean hasGlintOfCategory(Item item, GlintCategory category) {
        GlintDefinition def = REGISTRY.get(item);
        return def != null && def.category() == category;
    }

    /**
     * Returns all registered glints belonging to the specified category.
     *
     * @param category The category to filter by.
     * @return An unmodifiable map of items → definitions for the given category.
     */
    public static Map<Item, GlintDefinition> getByCategory(GlintCategory category) {
        Map<Item, GlintDefinition> result = new LinkedHashMap<>();
        REGISTRY.forEach((item, def) -> {
            if (def.category() == category) result.put(item, def);
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return An unmodifiable view of the entire registry.
     */
    public static Map<Item, GlintDefinition> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}