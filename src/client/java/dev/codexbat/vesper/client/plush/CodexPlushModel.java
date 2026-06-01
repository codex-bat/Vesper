package dev.codexbat.vesper.client.plush;

import java.util.List;

import static dev.codexbat.vesper.client.plush.FaceUV.of;

/**
 * Model data for "vesper:codex_plush", translated from the Blockbench JSON.
 *
 * To add a NEW plushie model for your own mod:
 *   1. Copy this file, adjust the cuboid data to match your Blockbench JSON.
 *   2. Register it: PlushyModelRegistry.register(yourId, YourModel.MODEL);
 *      (client-side, in your ClientModInitializer)
 *
 * If a face has texture "#missing" in Blockbench, pass null for that FaceUV.
 * 
 * But please note the license of this mod none-the-less. This is more for educational purposes.
 */
public final class CodexPlushModel {

    public static final PlushyModelData MODEL = build();

    private static PlushyModelData build() {

        // ── head ──────────────────────────────────────────────────────────────
        var head = new PlushyCuboid(
                4, 5, 5,  12, 13, 11,
                7, 6, 7,  0, 0, 0,
                of(1.5f, 1.5f, 3.5f, 3.5f),  // north
                of(0,    1.5f, 1.5f, 3.5f),  // east
                of(5,    1.5f, 7,    3.5f),  // south
                of(3.5f, 1.5f, 5,   3.5f),  // west
                of(3.5f, 1.5f, 1.5f, 0),    // up   (u reversed = mirrored)
                of(5.5f, 0,    3.5f, 1.5f)  // down (u reversed = mirrored)
        );

        // ── headwear ──────────────────────────────────────────────────────────
        var headwear = new PlushyCuboid(
                3.75f, 4.75f, 4.75f,  12.25f, 13.25f, 11.25f,
                7.25f, 5.75f, 7.25f,  0, 0, 0,
                of(1.5f, 5,    3.5f, 7),
                of(0,    5,    1.5f, 7),
                of(5,    5,    7,    7),
                of(3.5f, 5,    5,    7),
                of(3.5f, 5,    1.5f, 3.5f),
                of(5.5f, 3.5f, 3.5f, 5)
        );

        // ── body ──────────────────────────────────────────────────────────────
        var body = new PlushyCuboid(
                5.8f, 1, 6.5f,  10.3f, 8, 9.5f,
                6.25f, 2, 6.5f,  0, 0, 0,
                of(0,    7,     1.25f, 8.75f),  // north
                of(2.75f,7,     3.5f,  8.75f),  // east
                of(7,    0,     8.25f, 1.75f),  // south
                of(7,    2.75f, 7.75f, 4.5f),  // west
                null, null                       // up/down → #missing
        );

        // ── butt ──────────────────────────────────────────────────────────────
        var butt = new PlushyCuboid(
                5.4f, 0, 6,  10.9f, 2.5f, 10,
                5.85f, 1, 6,  0, 0, 0,
                of(3.5f, 7,    5,     7.75f),
                of(7,    6,    8,     6.75f),
                of(7,    4.5f, 8.5f,  5.25f),
                of(6.25f,7,    7.25f, 7.75f),
                of(2.75f,8,    1.25f, 7),      // up  (u reversed)
                of(8.5f, 1.75f,7,    2.75f)   // down (u reversed)
        );

        // ── left_leg (Y +22.5° around its origin) ────────────────────────────
        var leftLeg = new PlushyCuboid(
                4.25f, 0, 5,  6.25f, 2, 9,
                4.25f, 0, 5,  0, 22.5f, 0,
                of(8.25f, 1,     8.75f, 1.5f),
                of(7.25f, 6.75f, 8.25f, 7.25f),
                null,                                 // south → #missing
                of(7.25f, 7.25f, 8.25f, 7.75f),
                of(8.25f, 3.75f, 7.75f, 2.75f),     // up (u reversed)
                of(4,     7.75f, 3.5f,  8.75f)      // down (u reversed)
        );

        // ── right_leg (Y −22.5° around its origin) ───────────────────────────
        var rightLeg = new PlushyCuboid(
                10, 0, 4,  12, 2, 8,
                10, 0, 4,  0, -22.5f, 0,
                of(8.25f, 3.25f, 8.75f, 3.75f),
                of(7.75f, 3.75f, 8.75f, 4.25f),
                null,                                 // south → #missing
                of(4,     7.75f, 5,     8.25f),
                of(5.5f,  8.75f, 5,     7.75f),     // up (u reversed)
                of(6,     7.75f, 5.5f,  8.75f)
        );

        // ── left_arm (Y +90° around [2, 3.2, 9.3]) ───────────────────────────
        var leftArm = new PlushyCuboid(
                2.5f, 3.2f, 9.3f,  4, 4.7f, 13.3f,
                2, 3.2f, 9.3f,  0, 90, 0,
                of(4.5f, 8.25f, 5,    8.75f),
                of(6,    7.75f, 7,    8.25f),
                null,                               // south → #missing
                of(7,    7.75f, 8,    8.25f),
                of(1.75f,9,     1.25f,8),          // up (u reversed)
                of(2.25f,8,     1.75f,9)
        );

        // ── left_sleeves (no rotation) ────────────────────────────────────────
        var leftSleeves = new PlushyCuboid(
                1.75f, 3, 7,  2.75f, 5, 9,
                1.75f, 3, 7,  0, 0, 0,
                of(2.5f, 9.5f, 2.75f,10),
                of(2,    9.5f, 2.5f, 10),
                of(3.25f,9.5f, 3.5f, 10),
                of(2.75f,9.5f, 3.25f,10),
                of(2.75f,9.5f, 2.5f, 9),
                of(3,    9,    2.75f,9.5f)
        );

        // ── right_arm (Y −90° around [14, 3.2, 7.2]) ─────────────────────────
        var rightArm = new PlushyCuboid(
                14, 3.2f, 7.2f,  15.5f, 4.7f, 11.2f,
                14, 3.2f, 7.2f,  0, -90, 0,
                of(6,    8.25f, 6.5f, 8.75f),
                of(8,    6,     9,    6.5f),
                null,                               // south → #missing
                of(8,    7.75f, 9,    8.25f),
                of(2.75f,9,     2.25f,8),          // up (u reversed)
                of(8.75f,0,     8.25f,1)
        );

        // ── right_sleeves (no rotation) ───────────────────────────────────────
        var rightSleeves = new PlushyCuboid(
                12.25f, 3, 7,  13.25f, 5, 9,
                12.25f, 3, 7,  0, 0, 0,
                of(0.5f, 9.5f, 0.75f,10),
                of(0,    9.5f, 0.5f, 10),
                of(1.25f,9.5f, 1.5f, 10),
                of(0.75f,9.5f, 1.25f,10),
                of(0.75f,9.5f, 0.5f, 9),
                of(1,    9,    0.75f,9.5f)
        );

        // ── Assemble ──────────────────────────────────────────────────────────
        var staticBones = List.of(
                new PlushyBone("head",      List.of(head),               0, 0, 0),
                new PlushyBone("headwear",  List.of(headwear),           0, 0, 0),
                new PlushyBone("body",      List.of(body),               0, 0, 0),
                new PlushyBone("butt",      List.of(butt),               0, 0, 0),
                new PlushyBone("left_leg",  List.of(leftLeg),            0, 0, 0),
                new PlushyBone("right_leg", List.of(rightLeg),           0, 0, 0)
        );

        // Animation pivot = shoulder attachment point in pixel space
        var leftArmBone  = new PlushyBone("left_arm",  List.of(leftArm,  leftSleeves),  2,  3.2f, 9.3f);
        var rightArmBone = new PlushyBone("right_arm", List.of(rightArm, rightSleeves), 14, 3.2f, 7.2f);

        return new PlushyModelData(staticBones, leftArmBone, rightArmBone);
    }
}