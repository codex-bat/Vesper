package dev.codexbat.vesper.client.plush;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ── Value types ──────────────────────────────────────────────────────────────

/**
 * Per-face UV rectangle in the Blockbench item-model coordinate space (0–16).
 * Reversed UV (u0 > u1 or v0 > v1) is passed through as-is: the renderer
 * treats it as a mirror, exactly like Minecraft's item model system does.
 * Use {@code null} (not this record) for "#missing" faces.
 */
record FaceUV(float u0, float v0, float u1, float v1) {
    static FaceUV of(float u0, float v0, float u1, float v1) {
        return new FaceUV(u0, v0, u1, v1);
    }
}

/**
 * One Blockbench element — a cuboid with its own optional rotation.
 * All coordinates are in pixel space (divide by 16 to get block units).
 * {@code null} face = not rendered.
 */
record PlushyCuboid(
        // Bounding box
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        // Element-local rotation (pivot + Euler degrees)
        float pivX, float pivY, float pivZ,
        float rotX, float rotY, float rotZ,
        // Per-face UVs — null means skip that face
        FaceUV north, FaceUV east, FaceUV south, FaceUV west, FaceUV up, FaceUV down
) {}

/**
 * A named group of cuboids that share an animation pivot.
 * Static bones (body, head, legs) need no animation pivot; pass 0 for all three.
 * Arm bones use the shoulder attachment point as their animation pivot.
 */
record PlushyBone(
        String name,
        List<PlushyCuboid> cuboids,
        /** Animation pivot in pixel space — the point rotated when animating this bone. */
        float animPivX, float animPivY, float animPivZ
) {}

/**
 * Complete model description for one plushie.
 * Arm bones are kept separate so the renderer can apply the droop animation to them.
 */
record PlushyModelData(
        List<PlushyBone> staticBones,
        PlushyBone       leftArmBone,
        PlushyBone       rightArmBone
) {}

// ── Registry ─────────────────────────────────────────────────────────────────

/**
 * Maps plushie definition IDs → model data. Can be registered client-side in
 * ClientModInitializer (or via PlushyClientRegistry).
 */
public final class PlushyModelRegistry {
    private static final Map<Identifier, PlushyModelData> MODELS = new HashMap<>();

    public static void register(Identifier definitionId, PlushyModelData model) {
        MODELS.put(definitionId, model);
    }

    /** Returns null when no model is registered for this id. */
    public static PlushyModelData get(Identifier id) { return MODELS.get(id); }
}