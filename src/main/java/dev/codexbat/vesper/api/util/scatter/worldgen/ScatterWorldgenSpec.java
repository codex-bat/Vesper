package dev.codexbat.vesper.api.util.scatter.worldgen;

/**
 * Immutable description of a scatter rule.
 * This is data only: no registry writes, {@code no generation side effects}.
 */
public sealed interface ScatterWorldgenSpec
        permits BlockScatterSpec, CollectionScatterSpec, StructureScatterSpec {
}