package dev.codexbat.vesper.client.mixin.glint;

import dev.codexbat.vesper.api.glint.GlintDefinition;
import dev.codexbat.vesper.api.glint.VesperGlintRegistry;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleManager;
import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * All offsets passed to emitForStack() are from the entity's FEET (getY())
 * in world-axis-aligned space. WorldGlintParticle adds these to the entity's
 * interpolated position each render frame, so particles track the entity
 * regardless of camera position or movement.
 *
 * The offset is baked at spawn time in world-axis space based on the entity's
 * current yaw/pitch. Rotation between ticks produces minor drift for old
 * particles, but with short lifetimes (~7 ticks) this is imperceptible.
 */
@Mixin(ClientWorld.class)
public class WorldGlintEmitterMixin {

    @Inject(method = "tickEntities", at = @At("TAIL"))
    private void vesper$emitWorldGlintParticles(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        ClientWorld world = (ClientWorld) (Object) this;

        for (PlayerEntity player : world.getPlayers()) {
            emitHeldItems(client, player);
            emitArmorItems(player);
        }

        for (ItemEntity itemEntity : world.getEntitiesByClass(
                ItemEntity.class,
                client.player.getBoundingBox().expand(32.0),
                e -> true)) {
            // 0.25 above feet centres on the floating item model.
            emitForStack(itemEntity.getStack(), itemEntity, 0.0, 0.25, 0.0);
        }
    }

    private static void emitHeldItems(MinecraftClient client, PlayerEntity player) {
        boolean isLocalPlayer = player.getUuid().equals(client.player.getUuid());
        boolean isFirstPerson = client.options.getPerspective() == Perspective.FIRST_PERSON;

        if (isLocalPlayer && isFirstPerson) {
            emitHeldItemsFirstPerson(client.player);
        } else {
            emitHeldItemsThirdPerson(player);
        }
    }

    // -------------------------------------------------------------------------
    // FIRST-PERSON
    // -------------------------------------------------------------------------

    /**
     * All Y values are relative to player feet. getEyeHeight(pose) is
     * pose-aware: 1.62 standing, ~1.27 crouching, ~0.4 swimming/gliding.
     *
     * SWIMMING / GLIDING: the camera tilts to follow the look direction and
     * the arms extend forward, so we project along the 3D look vector.
     *
     * STANDING / CROUCHING: the hand is screen-anchored, not world-projected.
     * We use flat XZ forward + XZ lateral. Grip drop is pitch-based: looking
     * down raises the hand on screen (smaller drop); looking up lowers it.
     */
    private static void emitHeldItemsFirstPerson(ClientPlayerEntity player) {
        EntityPose pose = player.getPose();

        float yawRad = (float) Math.toRadians(player.getYaw());
        float sinYaw = (float) Math.sin(yawRad);
        float cosYaw = (float) Math.cos(yawRad);

        // Pose-aware eye height above feet.
        float eyeHeight = player.getEyeHeight(pose);

        if (pose == EntityPose.SWIMMING || pose == EntityPose.GLIDING) {
            emitHeldItemsFirstPersonProne(player, sinYaw, cosYaw, eyeHeight);
            return;
        }

        // Standard upright first-person.
        float pitch        = player.getPitch();
        float pitchClamped = Math.max(-90f, Math.min(90f, pitch));
        float pitchBlend   = (pitchClamped + 90f) / 180f; // 0=up, 0.5=horizon, 1=down

        // Grip drop from eye level:
        //   pitch -90 (up):   0.50
        //   pitch   0 (horiz):0.30
        //   pitch +90 (down): 0.05
        double gripDrop = 0.50 - (0.45 * pitchBlend);

        double fwdDist = 0.35;
        double fwdX    = -sinYaw * fwdDist;
        double fwdZ    =  cosYaw * fwdDist;

        float downFrac = Math.max(0f, pitchClamped / 90f);

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) continue;

            boolean isMainHand  = hand == Hand.MAIN_HAND;
            boolean rightHanded = player.getMainArm() == Arm.RIGHT;
            double  lateralSign = (isMainHand == rightHanded) ? 1.0 : -1.0;

            // Lateral collapses slightly when looking straight down.
            double latDist = 0.38 - (0.12 * downFrac);
            double latX    = -cosYaw * latDist * lateralSign;
            double latZ    = -sinYaw * latDist * lateralSign;

            emitForStack(stack, player,
                    fwdX + latX,
                    eyeHeight - gripDrop,  // from player feet
                    fwdZ + latZ);
        }
    }

    /**
     * First-person prone (SWIMMING or GLIDING).
     *
     * Camera tilts to follow look direction; arms extend forward along the
     * 3D look vector from the body centre. Lateral collapses toward zero
     * as pitch steepens (hands converge ahead of the body when diving).
     */
    private static void emitHeldItemsFirstPersonProne(
            ClientPlayerEntity player,
            float sinYaw, float cosYaw,
            float eyeHeight) {

        float pitchRad = (float) Math.toRadians(player.getPitch());
        float sinPitch = (float) Math.sin(pitchRad);
        float cosPitch = (float) Math.cos(pitchRad);

        float pitch        = player.getPitch();
        float pitchClamped = Math.max(-90f, Math.min(90f, pitch));
        float downFrac     = Math.max(0f, pitchClamped / 90f);

        // Arms reach 0.5 blocks forward along the full 3D look direction.
        double fwdDist     = 0.50;
        double fwdX        = -sinYaw * cosPitch * fwdDist;
        double fwdYOffset  = -sinPitch * fwdDist; // vertical component of reach
        double fwdZ        =  cosYaw * cosPitch * fwdDist;

        // Lateral is minimal (arms converge forward when swimming).
        // Collapses further when looking straight down.
        double latDist = 0.15 * (1.0 - downFrac * 0.8);

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) continue;

            boolean isMainHand  = hand == Hand.MAIN_HAND;
            boolean rightHanded = player.getMainArm() == Arm.RIGHT;
            double  lateralSign = (isMainHand == rightHanded) ? 1.0 : -1.0;

            double latX = -cosYaw * latDist * lateralSign;
            double latZ = -sinYaw * latDist * lateralSign;

            emitForStack(stack, player,
                    fwdX + latX,
                    eyeHeight + fwdYOffset,  // from player feet
                    fwdZ + latZ);
        }
    }

    // -------------------------------------------------------------------------
    // THIRD-PERSON / REMOTE PLAYERS
    // -------------------------------------------------------------------------

    private static void emitHeldItemsThirdPerson(PlayerEntity player) {
        switch (player.getPose()) {
            case SWIMMING    -> emitHeldItemsProne(player, false); // swimming + crawling
            case GLIDING     -> emitHeldItemsProne(player, true);  // elytra
            case SPIN_ATTACK -> emitHeldItemsRiptide(player);
            case SLEEPING    -> emitHeldItemsSleeping(player);
            case CROUCHING   -> emitHeldItemsUpright(player, true);
            default          -> emitHeldItemsUpright(player, false);
        }
    }

    /**
     * Upright pose (standing or crouching). All offsets are from entity feet.
     * Uses getBodyYaw() so the hand position tracks the body, not the head,
     * matching what is visible in third-person.
     */
    private static void emitHeldItemsUpright(PlayerEntity player, boolean crouching) {
        float yawRad = (float) Math.toRadians(player.getBodyYaw());
        float sinYaw = (float) Math.sin(yawRad);
        float cosYaw = (float) Math.cos(yawRad);

        double heightFraction = crouching ? 0.52 : 0.62;
        double offsetY        = player.getHeight() * heightFraction; // from feet

        // Crouching model leans slightly forward.
        double forwardBias = crouching ? 0.12 : 0.0;
        double biasX       = -sinYaw * forwardBias;
        double biasZ       =  cosYaw * forwardBias;

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) continue;

            boolean isMainHand  = hand == Hand.MAIN_HAND;
            boolean rightHanded = player.getMainArm() == Arm.RIGHT;
            double  lateralSign = (isMainHand == rightHanded) ? 1.0 : -1.0;

            double offsetX = biasX + (-sinYaw * 0.20) + (-cosYaw * 0.30 * lateralSign);
            double offsetZ = biasZ + ( cosYaw * 0.20) + (-sinYaw * 0.30 * lateralSign);

            emitForStack(stack, player, offsetX, offsetY, offsetZ);
        }
    }

    /**
     * Prone pose (SWIMMING / crawling, GLIDING). Body is horizontal along
     * the 3D look vector. Offsets are from entity feet in world-axis space.
     *
     * The "right" vector (arm lateral) is the XZ perpendicular to yaw and
     * is pitch-independent — arms stay to the body's sides regardless of
     * dive angle.
     */
    private static void emitHeldItemsProne(PlayerEntity player, boolean foldedArms) {
        float yawRad   = (float) Math.toRadians(player.getYaw());
        float pitchRad = (float) Math.toRadians(player.getPitch());
        float sinYaw   = (float) Math.sin(yawRad);
        float cosYaw   = (float) Math.cos(yawRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosPitch = (float) Math.cos(pitchRad);

        // Body centre: height is compressed (0.6 when swimming/gliding).
        double bodyCentreY = player.getHeight() * 0.5;

        // Forward (3D look direction) and right (XZ perpendicular) vectors.
        double fwdX    = -sinYaw * cosPitch;
        double fwdY    = -sinPitch;
        double fwdZ    =  cosYaw * cosPitch;
        double rightX  = -cosYaw; // player's right in XZ
        double rightZ  = -sinYaw;

        double fwdReach = foldedArms ? 0.10 : 0.25;
        double latReach = foldedArms ? 0.10 : 0.22;

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) continue;

            boolean isMainHand  = hand == Hand.MAIN_HAND;
            boolean rightHanded = player.getMainArm() == Arm.RIGHT;
            double  lateralSign = (isMainHand == rightHanded) ? 1.0 : -1.0;

            emitForStack(stack, player,
                    fwdX * fwdReach + rightX * latReach * lateralSign,
                    bodyCentreY + fwdY * fwdReach,
                    fwdZ * fwdReach + rightZ * latReach * lateralSign);
        }
    }

    private static void emitHeldItemsRiptide(PlayerEntity player) {
        float yawRad = (float) Math.toRadians(player.getYaw());
        float sinYaw = (float) Math.sin(yawRad);
        float cosYaw = (float) Math.cos(yawRad);

        ItemStack main = player.getMainHandStack();
        if (!main.isEmpty()) {
            emitForStack(main, player,
                    -sinYaw * 0.5,
                    player.getHeight() * 0.70,
                    cosYaw * 0.5);
        }
    }

    private static void emitHeldItemsSleeping(PlayerEntity player) {
        float yawRad = (float) Math.toRadians(player.getYaw());
        float sinYaw = (float) Math.sin(yawRad);
        float cosYaw = (float) Math.cos(yawRad);

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) continue;

            boolean isMainHand  = hand == Hand.MAIN_HAND;
            boolean rightHanded = player.getMainArm() == Arm.RIGHT;
            double  lateralSign = (isMainHand == rightHanded) ? 1.0 : -1.0;

            emitForStack(stack, player,
                    (-sinYaw * 0.35) + (-cosYaw * 0.18 * lateralSign),
                    0.3,  // above entity feet (bed surface height)
                    ( cosYaw * 0.35) + (-sinYaw * 0.18 * lateralSign));
        }
    }

    // -------------------------------------------------------------------------
    // ARMOUR
    // -------------------------------------------------------------------------

    private static void emitArmorItems(LivingEntity entity) {
        EntityPose pose   = entity.getPose();
        boolean    prone  = pose == EntityPose.SWIMMING || pose == EntityPose.GLIDING;

        float yawRad   = (float) Math.toRadians(entity.getYaw());
        float pitchRad = prone ? (float) Math.toRadians(entity.getPitch()) : 0f;
        float sinYaw   = (float) Math.sin(yawRad);
        float cosYaw   = (float) Math.cos(yawRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosPitch = prone ? (float) Math.cos(pitchRad) : 1f;

        double bodyHeight = entity.getHeight();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            ItemStack stack = entity.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            double fraction = switch (slot) {
                case FEET  -> 0.10;
                case LEGS  -> 0.35;
                case CHEST -> 0.65;
                case HEAD  -> 0.90;
                default    -> 0.50;
            };

            if (!prone) {
                double sneakOffset = (pose == EntityPose.CROUCHING) ? -0.08 : 0.0;
                emitForStack(stack, entity,
                        0.0,
                        bodyHeight * fraction + sneakOffset,
                        0.0);
            } else {
                // Map slot fraction to position along the body spine.
                // fraction 0.9 (head) → front; 0.1 (feet) → tail.
                double along = fraction - 0.5; // -0.4 to +0.4
                emitForStack(stack, entity,
                        -sinYaw * cosPitch * along,
                        bodyHeight * 0.5 + (-sinPitch * along),
                        cosYaw * cosPitch * along);
            }
        }
    }

    // -------------------------------------------------------------------------
    // SHARED
    // -------------------------------------------------------------------------

    private static void emitForStack(ItemStack stack, Entity entity,
                                     double offsetX, double offsetY, double offsetZ) {
        if (stack.isEmpty() || entity.isRemoved()) return;

        Optional<GlintDefinition> opt = VesperGlintRegistry.getGlint(stack.getItem());
        if (opt.isEmpty()) return;

        GlintDefinition def = opt.get();
        if (!def.hasWorldParticles()) return;

        if (def instanceof AmbientGlint ambient) {
            WorldGlintParticleManager.spawn(entity, offsetX, offsetY, offsetZ, ambient.getConfig());
        } else if (def instanceof SparkleGlint sparkle) {
            WorldGlintParticleManager.spawn(entity, offsetX, offsetY, offsetZ, sparkle.getWorldConfig());
        }
    }
}