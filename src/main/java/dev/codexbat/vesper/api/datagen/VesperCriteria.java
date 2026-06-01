package dev.codexbat.vesper.api.datagen;

import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Convenience factories for every common vanilla advancement criterion type.
 *
 * <p>Use inside a {@link VesperAdvancementProvider}:
 * <pre>
 * .criterion("get_diamond",  VesperCriteria.obtainItem(Items.DIAMOND))
 * .criterion("kill_zombie",  VesperCriteria.killEntity(EntityType.ZOMBIE))
 * .criterion("go_nether",    VesperCriteria.enterDimension(World.NETHER))
 * .criterion("never",        VesperCriteria.never())                // manually granted
 * .criterion("use_hoe",      VesperCriteria.useItemOnBlock(
 *       blockLookup, itemLookup, Blocks.DIRT,
 *       ItemPredicate.Builder.create().items(itemLookup, Items.WOODEN_HOE)))
 * .criterion("craft_table",  VesperCriteria.craftRecipe(RegistryKey.of(RegistryKeys.RECIPE, Identifier.of("crafting_table"))))
 * .criterion("shoot_arrow",  VesperCriteria.shootCrossbow(itemLookup, Items.ARROW))
 * </pre>
 *
 * <p><b>Note:</b> Some methods require {@link RegistryEntryLookup} parameters because
 * Minecraft 1.21.x uses registry entries directly. Pass the lookups from
 * {@code ProviderContext} (e.g. {@code context.getRegistries()}).
 */
public final class VesperCriteria {

    private VesperCriteria() {}

    // ═════════════════════════════════════════════════════════════════════════
    //  Items / Inventory
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player's inventory contains {@code item}. */
    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> obtainItem(
            ItemConvertible item) {
        return InventoryChangedCriterion.Conditions.items(item);
    }

    /** Fires when the inventory matches all supplied {@link ItemPredicate}s. */
    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> obtainItem(
            ItemPredicate... predicates) {
        return InventoryChangedCriterion.Conditions.items(predicates);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Entities — kills
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player kills an entity of the given type. */
    public static AdvancementCriterion<OnKilledCriterion.Conditions> killEntity(
            EntityType<?> type) {
        return OnKilledCriterion.Conditions.createPlayerKilledEntity(
                EntityPredicate.Builder.create()
                        .type(new EntityTypePredicate(RegistryEntryList.of(type.getRegistryEntry())))
        );
    }

    /** Fires when the player kills any entity (no type filter). */
    public static AdvancementCriterion<OnKilledCriterion.Conditions> killAnyEntity() {
        return OnKilledCriterion.Conditions.createPlayerKilledEntity();
    }

    /** Fires when the player is killed by an entity of the given type. */
    public static AdvancementCriterion<OnKilledCriterion.Conditions> killedByEntity(
            EntityType<?> killerType) {
        return OnKilledCriterion.Conditions.createEntityKilledPlayer(
                EntityPredicate.Builder.create()
                        .type(new EntityTypePredicate(RegistryEntryList.of(killerType.getRegistryEntry())))
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Entities — damage
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player deals damage to the given entity type. */
    public static AdvancementCriterion<PlayerHurtEntityCriterion.Conditions> hurtEntity(
            EntityType<?> type) {
        return PlayerHurtEntityCriterion.Conditions.createEntity(
                Optional.of(
                        EntityPredicate.Builder.create()
                                .type(new EntityTypePredicate(RegistryEntryList.of(type.getRegistryEntry())))
                                .build()
                )
        );
    }

    /** Fires when the player receives damage from an entity of the given type. */
    public static AdvancementCriterion<EntityHurtPlayerCriterion.Conditions> hurtByEntity(
            EntityType<?> sourceType) {
        // EntityHurtPlayerCriterion uses DamagePredicate, not a direct entity predicate
        DamagePredicate.Builder damageBuilder = DamagePredicate.Builder.create()
                .sourceEntity(EntityPredicate.Builder.create()
                        .type(new EntityTypePredicate(RegistryEntryList.of(sourceType.getRegistryEntry()))).build());
        return EntityHurtPlayerCriterion.Conditions.create(damageBuilder);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Blocks
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player places the given block. */
    public static AdvancementCriterion<ItemCriterion.Conditions> placeBlock(Block block) {
        return ItemCriterion.Conditions.createPlacedBlock(block);
    }

    /** Fires when the player enters (stands inside) the given block. */
    public static AdvancementCriterion<EnterBlockCriterion.Conditions> enterBlock(Block block) {
        return EnterBlockCriterion.Conditions.block(block);
    }

    /**
     * Fires when an item is used on the given block.
     *
     * @param blockLookup the block registry lookup (from the provider context)
     * @param itemLookup  the item registry lookup (from the provider context)
     * @param block       the targeted block
     * @param item        an {@code ItemPredicate.Builder} describing the used item
     */
    public static AdvancementCriterion<ItemCriterion.Conditions> useItemOnBlock(
            RegistryEntryLookup<Block> blockLookup,
            RegistryEntryLookup<net.minecraft.item.Item> itemLookup,
            Block block,
            ItemPredicate.Builder item) {
        LocationPredicate.Builder location = LocationPredicate.Builder.create()
                .block(net.minecraft.predicate.BlockPredicate.Builder.create()
                        .blocks(blockLookup, block));
        return ItemCriterion.Conditions.createItemUsedOnBlock(location, item);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  World / Dimensions
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player travels to the given dimension. */
    public static AdvancementCriterion<ChangedDimensionCriterion.Conditions> enterDimension(
            RegistryKey<World> dimension) {
        return ChangedDimensionCriterion.Conditions.create(null, dimension);
    }

    /** Fires when the player travels from {@code from} to {@code to}. */
    public static AdvancementCriterion<ChangedDimensionCriterion.Conditions> changeDimension(
            RegistryKey<World> from, RegistryKey<World> to) {
        return ChangedDimensionCriterion.Conditions.create(from, to);
    }

    /** Fires on any dimension change. */
    public static AdvancementCriterion<ChangedDimensionCriterion.Conditions> anyDimensionChange() {
        return ChangedDimensionCriterion.Conditions.create(null, null);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Player state
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires every tick. Good for root/hidden advancements. */
    public static AdvancementCriterion<TickCriterion.Conditions> tick() {
        return TickCriterion.Conditions.createTick();
    }

    /** Never fires – use for manually granted advancements. */
    public static AdvancementCriterion<ImpossibleCriterion.Conditions> never() {
        return Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Animals
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player tames the given entity type. */
    public static AdvancementCriterion<TameAnimalCriterion.Conditions> tameAnimal(
            EntityType<?> type) {
        return TameAnimalCriterion.Conditions.create(
                EntityPredicate.Builder.create()
                        .type(new EntityTypePredicate(RegistryEntryList.of(type.getRegistryEntry())))
        );
    }

    /** Fires when the player breeds two animals of the given types. */
    public static AdvancementCriterion<BredAnimalsCriterion.Conditions> breedAnimals(
            EntityType<?> parent, EntityType<?> partner) {
        return BredAnimalsCriterion.Conditions.create(
                Optional.of(
                        EntityPredicate.Builder.create()
                                .type(new EntityTypePredicate(RegistryEntryList.of(parent.getRegistryEntry())))
                                .build()
                ),
                Optional.of(
                        EntityPredicate.Builder.create()
                                .type(new EntityTypePredicate(RegistryEntryList.of(partner.getRegistryEntry())))
                                .build()
                ),
                Optional.empty()
        );
    }

    /** Fires when the player breeds any two animals. */
    public static AdvancementCriterion<BredAnimalsCriterion.Conditions> breedAnyAnimals() {
        return BredAnimalsCriterion.Conditions.create(
                Optional.empty(), Optional.empty(), Optional.empty()
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Crafting / Trading
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player crafts the recipe with the given key. */
    public static AdvancementCriterion<RecipeCraftedCriterion.Conditions> craftRecipe(
            RegistryKey<Recipe<?>> recipeKey) {
        return RecipeCraftedCriterion.Conditions.create(recipeKey);
    }

    /** Fires when the player completes a trade with any villager. */
    public static AdvancementCriterion<VillagerTradeCriterion.Conditions> tradeWithVillager() {
        return VillagerTradeCriterion.Conditions.any();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Misc vanilla
    // ═════════════════════════════════════════════════════════════════════════

    /** Fires when the player sleeps in a bed. */
    public static AdvancementCriterion<TickCriterion.Conditions> sleepInBed() {
        return TickCriterion.Conditions.createSleptInBed();
    }

    /** Fires when the player constructs a beacon at the given power level. */
    public static AdvancementCriterion<ConstructBeaconCriterion.Conditions> buildBeacon(
            int level) {
        return ConstructBeaconCriterion.Conditions.level(NumberRange.IntRange.atLeast(level));
    }

    /** Fires when the player shoots a crossbow with the given item. */
    public static AdvancementCriterion<ShotCrossbowCriterion.Conditions> shootCrossbow(
            RegistryEntryLookup<net.minecraft.item.Item> itemRegistry,
            ItemConvertible item) {
        return ShotCrossbowCriterion.Conditions.create(itemRegistry, item);
    }

    /**
     * Custom / modded criterion by its registered id.
     *
     * [⚠️] You must pass a valid conditions instance.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CriterionConditions> AdvancementCriterion<T> custom(
            String criterionId, T conditions) {
        var criterion = (Criterion<T>) Registries.CRITERION.get(Identifier.of(criterionId));
        if (criterion == null) {
            throw new IllegalArgumentException("Unknown criterion: " + criterionId);
        }
        return criterion.create(conditions);
    }
}