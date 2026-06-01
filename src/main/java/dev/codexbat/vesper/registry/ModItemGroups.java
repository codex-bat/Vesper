package dev.codexbat.vesper.registry;

import dev.codexbat.vesper.Vesper;
import dev.codexbat.vesper.plush.PlushyDefinition;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    private ModItemGroups() {}

    public static void initialize() {
        Item plushyIcon = PlushyRegistry.getItem(Vesper.id("codex_plush"));

        ItemGroup VESPER_TAB = FabricItemGroup.builder()
                .icon(() -> new ItemStack(plushyIcon))
                .displayName(Text.translatable("itemGroup.vesper.plushies"))
                .entries((context, entries) -> {
                    for (PlushyDefinition def : PlushyRegistry.getAllDefinitions()) {
                        Item item = PlushyRegistry.getItem(def.getId());
                        if (item != null) entries.add(item);
                    }
                })
                .build();

        Registry.register(Registries.ITEM_GROUP, Vesper.id("vesper_tab"), VESPER_TAB);
    }
}