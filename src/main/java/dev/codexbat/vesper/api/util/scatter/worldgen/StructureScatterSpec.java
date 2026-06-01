package dev.codexbat.vesper.api.util.scatter.worldgen;

import net.minecraft.util.Identifier;

/**
 * For structure-style scattering where the final worldgen class decides
 * how the structure is actually placed.
 */
public record StructureScatterSpec(
        Identifier structureId,
        int radius,
        float density,
        int attemptsPerChunk,
        int minY,
        int maxY
) implements ScatterWorldgenSpec {
}