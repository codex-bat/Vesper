package dev.codexbat.vesper.datagen.provider;

import dev.codexbat.vesper.Vesper;
import dev.codexbat.vesper.api.datagen.VesperBlockLootProvider;
import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.plush.loot.PlushyDropLootFunction;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public final class ModBlockLootProvider extends VesperBlockLootProvider {

    public ModBlockLootProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registries
    ) {
        super(output, registries);
    }

    @Override
    public void generate() {
        addDrop(
                PlushyRegistry.PLUSHY_BLOCK,
                LootTable.builder()
                        .pool(LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1))
                                .with(ItemEntry.builder(
                                        PlushyRegistry.getItem(Vesper.id("codex_plush"))
                                ).apply(PlushyDropLootFunction.builder()))
                                .conditionally(SurvivesExplosionLootCondition.builder()))
        );
    }
}