package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Implement this to hook mod's data providers into Vesper's
 * data generation pipeline.
 *
 * <p><b>Setup in dependent mod's fabric.mod.json:</b>
 * <pre>
 * "entrypoints": {
 *   "vesper-datagen": ["com.example.hisMod.datagen.hisModDatagenPlugin"]
 * }
 * </pre>
 *
 * <p><b>Example implementation:</b>
 * <pre>
 * public class hisModDatagenPlugin implements VesperDatagenPlugin {
 *     {@literal @}Override
 *     public void buildDatagen(FabricDataGenerator.Pack pack) {
 *         pack.addProvider(hisAdvancementProvider::new);
 *         pack.addProvider(hisBlockLootProvider::new);
 *         pack.addProvider(hisModelProvider::new);
 *     }
 * }
 * </pre>
 *
 * <p>If you'd rather keep fully independent datagen (perfectly valid!),
 * just create your own {@code DataGeneratorEntrypoint} and use the
 * {@code api.*} provider base-classes directly — no plugin needed.
 */
@FunctionalInterface
public interface VesperDatagenPlugin {
    void buildDatagen(FabricDataGenerator.Pack pack);
}

/**
 * Potential improvement:
 *
 * public interface VesperDatagenPlugin {
 *     void buildDatagen(VesperDatagenContext context);
 * }
 *
 * this is only here so I can comment freely :3
 */
interface NothingBurger {
    default void nothing() {};
}