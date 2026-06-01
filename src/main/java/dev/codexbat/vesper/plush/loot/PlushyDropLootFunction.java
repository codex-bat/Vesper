package dev.codexbat.vesper.plush.loot;

import com.mojang.serialization.MapCodec;
import dev.codexbat.vesper.plush.PlushyRegistry;
import dev.codexbat.vesper.plush.block.entity.PlushyBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameter;

import java.util.Set;

public class PlushyDropLootFunction implements LootFunction {

    public static final MapCodec<PlushyDropLootFunction> CODEC = MapCodec.unit(PlushyDropLootFunction::new);
    public static final LootFunctionType<PlushyDropLootFunction> TYPE = new LootFunctionType<>(CODEC);

    @Override
    public LootFunctionType<PlushyDropLootFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        BlockEntity be = context.get(LootContextParameters.BLOCK_ENTITY);
        if (be instanceof PlushyBlockEntity plushy) {
            Identifier definitionId = plushy.getDefinitionId();
            if (definitionId != null) {
                ItemStack newStack = new ItemStack(PlushyRegistry.getItem(definitionId));
                newStack.setCount(stack.getCount());
                return newStack;
            }
        }
        return stack;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.BLOCK_ENTITY);
    }

    public static LootFunction.Builder builder() {
        return PlushyDropLootFunction::new;
    }
}