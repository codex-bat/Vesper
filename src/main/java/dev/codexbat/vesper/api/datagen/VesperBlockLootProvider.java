package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for block loot table providers. Extend this and implement
 * {@link #generate()}, then call the helpers below or any of the many methods
 * inherited from {@link FabricBlockLootTableProvider} / {@code BlockLootTableGenerator}.
 *
 * <p><b>Inherited helpers you'll use constantly:</b>
 * <ul>
 *   <li>{@code addDrop(block)} — block always drops itself</li>
 *   <li>{@code addDrop(block, item)} — block always drops {@code item}</li>
 *   <li>{@code addDropWithSilkTouch(block, drop)} — drops {@code drop} only with silk touch</li>
 *   <li>{@code addDropWithSilkTouchOrNormally(block, silkDrop, normalDrop)} — bifurcated drop</li>
 *   <li>{@code dropsNothing(block)} — block drops nothing</li>
 *   <li>{@code addDrop(block, builder)} — fully custom loot table</li>
 * </ul>
 *
 * <p><b>Vesper extras (defined below):</b>
 * <ul>
 *   <li>{@code dropCount(item, n)} — always drop exactly n of item</li>
 *   <li>{@code dropBetween(item, min, max)} — random count in range</li>
 *   <li>{@code dropWithChance(block, item, chance)} — drop item with a flat probability</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <pre>
 * public class MyBlockLootProvider extends VesperBlockLootProvider {
 *     public MyBlockLootProvider(FabricDataOutput out, CompletableFuture&lt;...&gt; reg) {
 *         super(out, reg);
 *     }
 *
 *     {@literal @}Override
 *     public void generate() {
 *         addDrop(MyBlocks.OAK_CRATE);                            // drops itself
 *         addDrop(MyBlocks.ORE, MyItems.GEM);                     // always drops gem
 *         addDropWithSilkTouch(MyBlocks.GLASS_PANE, MyBlocks.GLASS_PANE);
 *         addDrop(MyBlocks.GRAVEL_BIN, dropBetween(Items.GRAVEL, 2, 6));
 *         dropWithChance(MyBlocks.RARE_ORE, MyItems.RARE_GEM, 0.15f); // 15% chance
 *     }
 * }
 * </pre>
 */
public abstract class VesperBlockLootProvider extends FabricBlockLootTableProvider {

    protected VesperBlockLootProvider(FabricDataOutput output,
                                      CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
        super(output, lookup);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Vesper extra helpers
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * A loot table builder that always drops exactly {@code count} of {@code item},
     * respecting the "survives explosion" condition (i.e. TNT may not always yield drops).
     */
    protected LootTable.Builder dropCount(ItemConvertible item, int count) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(item)
                                .apply(SetCountLootFunction.builder(
                                        ConstantLootNumberProvider.create(count)))
                                .conditionally(SurvivesExplosionLootCondition.builder())));
    }

    /**
     * A loot table builder that drops a random count of {@code item} between
     * {@code min} and {@code max} (inclusive, uniform distribution).
     */
    protected LootTable.Builder dropBetween(ItemConvertible item, float min, float max) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(item)
                                .apply(SetCountLootFunction.builder(
                                        UniformLootNumberProvider.create(min, max)))
                                .conditionally(SurvivesExplosionLootCondition.builder())));
    }

    /**
     * Registers a loot table where {@code block} has a flat {@code chance}
     * (0.0–1.0) of dropping {@code item}.
     * <pre>
     * dropWithChance(MyBlocks.RARE_ORE, MyItems.RARE_GEM, 0.10f); // 10% drop
     * </pre>
     */
    protected void dropWithChance(Block block, ItemConvertible item, float chance) {
        addDrop(block, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(item)
                                .conditionally(RandomChanceLootCondition.builder(chance))
                                .conditionally(SurvivesExplosionLootCondition.builder()))));
    }
}