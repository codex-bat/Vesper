package dev.codexbat.vesper.api.block.stackable;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;

public interface StackCountAccess {
    IntProperty vesper$getStackCountProperty();

    int vesper$getMinStackCount();

    int vesper$getMaxStackCount();

    default int vesper$getStackCount(BlockState state) {
        return state.get(vesper$getStackCountProperty());
    }

    default BlockState vesper$withStackCount(BlockState state, int count) {
        int clamped = Math.max(vesper$getMinStackCount(), Math.min(vesper$getMaxStackCount(), count));
        return state.with(vesper$getStackCountProperty(), clamped);
    }
}