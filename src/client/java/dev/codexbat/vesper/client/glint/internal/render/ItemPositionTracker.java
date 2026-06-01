package dev.codexbat.vesper.client.glint.internal.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the last known world-space position of each held item, written by
 * HeldItemRendererMixin on the render thread and read by WorldGlintEmitterMixin
 * on the tick thread.
 *
 * Positions are updated every rendered frame and are therefore always fresh
 * relative to the 20 ticks/sec read rate.
 *
 * The map key encodes both the player UUID and the hand so that main-hand and
 * off-hand positions are tracked independently per player.
 */
@Environment(EnvType.CLIENT)
public final class ItemPositionTracker {

    /**
     * Composite key: player UUID + hand ordinal.
     * Avoids allocating a wrapper object per lookup by packing into a long pair.
     */
    private record HandKey(UUID uuid, Hand hand) {}

    private static final ConcurrentHashMap<HandKey, Vec3d> POSITIONS = new ConcurrentHashMap<>();

    private ItemPositionTracker() {}

    /**
     * Records the world-space origin of the item held in the given hand.
     * Called from the render thread by HeldItemRendererMixin.
     *
     * @param playerUuid UUID of the player holding the item.
     * @param hand       Which hand (MAIN_HAND or OFF_HAND).
     * @param worldPos   World-space position of the item's render origin.
     */
    public static void put(UUID playerUuid, Hand hand, Vec3d worldPos) {
        POSITIONS.put(new HandKey(playerUuid, hand), worldPos);
    }

    /**
     * Returns the last recorded world-space position for the given player and
     * hand, or null if no position has been recorded yet (e.g. the hand is empty
     * or the player has not yet been rendered).
     *
     * @param playerUuid UUID of the player.
     * @param hand       Which hand to query.
     * @return World-space Vec3d, or null.
     */
    public static Vec3d get(UUID playerUuid, Hand hand) {
        return POSITIONS.get(new HandKey(playerUuid, hand));
    }

    /**
     * Removes all cached positions for a given player.
     * Should be called when the player leaves the world or is removed from
     * the entity list, to prevent stale entries accumulating.
     *
     * @param playerUuid UUID of the player to evict.
     */
    public static void evict(UUID playerUuid) {
        POSITIONS.remove(new HandKey(playerUuid, Hand.MAIN_HAND));
        POSITIONS.remove(new HandKey(playerUuid, Hand.OFF_HAND));
    }
}