package dev.codexbat.vesper.plush;

import dev.codexbat.vesper.plush.block.PlushyBlock;
import dev.codexbat.vesper.plush.block.entity.PlushyBlockEntity;
import dev.codexbat.vesper.plush.entity.PlushyEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Calling PlushyRegistry.initialize() once in common mod initializer,
 * then PlushyRegistry.register(definition) for each plushie.
 */
public final class PlushyRegistry {

    public static Block PLUSHY_BLOCK;
    public static BlockEntityType<PlushyBlockEntity> PLUSHY_BLOCK_ENTITY;

    private static final Map<Identifier, PlushyDefinition> DEFINITIONS = new HashMap<>();
    private static final Map<Identifier, Item>             ITEMS       = new HashMap<>();
    private static EntityType<PlushyEntity> ENTITY_TYPE;

    public static EntityType<PlushyEntity> getEntityType() {
        if (ENTITY_TYPE == null) throw new IllegalStateException("Call PlushyRegistry.initialize() first");
        return ENTITY_TYPE;
    }

    /** Register the shared entity type. Vesper calls this; you do not need to. */
    public static void initialize() {
        if (PLUSHY_BLOCK != null) {
            return;
        }

        Identifier id = Identifier.of("vesper", "plushy");
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);

        PLUSHY_BLOCK = Registry.register(
                Registries.BLOCK,
                id,
                new PlushyBlock(
                        AbstractBlock.Settings.create()
                                .registryKey(blockKey)
                                .strength(0.2f)
                                .sounds(BlockSoundGroup.WOOL)
                                .nonOpaque()
                )
        );

        PLUSHY_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.create(
                        PlushyBlockEntity::new,
                        PLUSHY_BLOCK
                ).build()
        );
    }

    /**
     * Register a plushie. Creates and registers its Item. Returns the item.
     * Called from common ModInitializer, after PlushyRegistry.initialize().
     */
    public static Item register(PlushyDefinition def) {
        Identifier id = def.getId();
        if (DEFINITIONS.containsKey(id))
            throw new IllegalArgumentException("Plushie already registered: " + id);

        DEFINITIONS.put(id, def);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        PlushyItem item = new PlushyItem(def, new Item.Settings().maxCount(1).registryKey(itemKey));
        Registry.register(Registries.ITEM, id, item);
        ITEMS.put(id, item);
        return item;
    }

    public static PlushyDefinition           getDefinition(Identifier id)  { return DEFINITIONS.get(id); }
    public static PlushyDefinition           getDefinition(String raw)      { return DEFINITIONS.get(Identifier.of(raw)); }
    public static Item                       getItem(Identifier id)         { return ITEMS.get(id); }
    public static Collection<PlushyDefinition> getAllDefinitions()          { return DEFINITIONS.values(); }
}