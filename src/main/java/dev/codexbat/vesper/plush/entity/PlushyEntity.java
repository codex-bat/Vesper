package dev.codexbat.vesper.plush.entity;

import com.mojang.serialization.Codec;
import dev.codexbat.vesper.plush.PlushyDefinition;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Non-living entity — no AI, no health, no attributes.
 *
 * Right-click  → squish scale + sound.
 * Any hit      → drop item + discard (creative: silent delete).
 * Collision    → AABB from EntityType.dimensions() in PlushyRegistry.
 */
public class PlushyEntity extends Entity {

    // ── Tracked data ──────────────────────────────────────────────────────────

    private static final TrackedData<String> DEFINITION_ID =
            DataTracker.registerData(PlushyEntity.class, TrackedDataHandlerRegistry.STRING);

    /**
     * Counts down {@link #SQUISH_DURATION} → 0 after each right-click.
     * Server writes it; both sides read it. Renderer normalises to 0–1.
     */
    private static final TrackedData<Integer> SQUISH_TICK =
            DataTracker.registerData(PlushyEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /** Duration of the squish animation in ticks. Public so the renderer can normalise. */
    public static final int SQUISH_DURATION = 10;

    // ── Client-side animation fields ──────────────────────────────────────────

    /** 0 = T-pose, 1 = arms fully drooped. */
    public float armProgress     = 0f;
    public float prevArmProgress = 0f;

    /** Incremented each tick; drives the idle bob sine. */
    public float bobPhase     = 0f;
    public float prevBobPhase = 0f;

    // ── Construction ─────────────────────────────────────────────────────────

    public PlushyEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(DEFINITION_ID, "");
        builder.add(SQUISH_TICK,   0);
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        prevArmProgress = armProgress;
        prevBobPhase    = bobPhase;

        // ── Arm droop: exponential ease-out ──────────────────────────────────
        // Closes 35 % of the remaining gap each tick.
        if (armProgress < 0.999f) {
            armProgress += (1f - armProgress) * 0.35f;
            if (armProgress > 0.999f) armProgress = 1f;
        }

        // Idle bob — full cycle ≈ 2.5 s at 20 TPS
        bobPhase += 0.08f;

        // Squish timer — server authoritative, client mirrors via TrackedData
        if (!this.getEntityWorld().isClient()) {
            int st = this.dataTracker.get(SQUISH_TICK);
            if (st > 0) this.dataTracker.set(SQUISH_TICK, st - 1);
        }
    }

    // ── Interaction (right-click) ─────────────────────────────────────────────

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getEntityWorld().isClient()) {
            this.dataTracker.set(SQUISH_TICK, SQUISH_DURATION);
            this.getEntityWorld().playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_WOOL_HIT,
                    SoundCategory.NEUTRAL,
                    0.7f,
                    0.9f + this.random.nextFloat() * 0.4f
            );
        }
        return ActionResult.SUCCESS;
    }

    // ── Damage / breaking ─────────────────────────────────────────────────────

    /**
     * Any hit destroys the plushie and drops the item — "breakable like wool."
     * Creative players delete it silently, matching vanilla block behaviour.
     */
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }

        if (!(source.getAttacker() instanceof PlayerEntity player && player.isCreative())) {
            PlushyDefinition def = getDefinition();
            if (def != null) {
                Item item = PlushyRegistry.getItem(def.getId());
                if (item != null) {
                    this.dropStack(world, new ItemStack(item));
                }
            }
        }
        this.discard();
        return true;
    }

    // ── Collision / interaction flags ─────────────────────────────────────────

    /** Other entities are blocked by this one; shape = EntityType AABB. */
    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        return true;
    }
    /** Cannot be pushed or knocked by anything. */
    @Override
    public boolean isPushable() {
        return false;
    }
    /** Needed so the client sends an attack packet → triggers damage() server-side. */
    @Override
    public boolean isAttackable() {
        return true;
    }
    /** Arrows / thrown items pass straight through. */
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    // ── Definition / squish access ────────────────────────────────────────────

    public void setDefinitionId(String id) {
        this.dataTracker.set(DEFINITION_ID, id);
    }

    public String getDefinitionId() {
        return this.dataTracker.get(DEFINITION_ID);
    }

    public PlushyDefinition getDefinition() {
        String raw = this.dataTracker.get(DEFINITION_ID);
        return raw.isEmpty() ? null : PlushyRegistry.getDefinition(raw);
    }

    /**
     * 1.0 right after a right-click, counting smoothly down to 0.0 over
     * {@link #SQUISH_DURATION} ticks. Used by the renderer to drive the
     * squeeze scale transform.
     */
    public float getSquishNorm() {
        return this.dataTracker.get(SQUISH_TICK) / (float) SQUISH_DURATION;
    }

    // ── NBT (custom data) ────────────────────────────────────────────────────

    @Override
    protected void readCustomData(ReadView view) {
        Optional<String> defId = view.read("DefinitionId", Codec.STRING);
        this.setDefinitionId(defId.orElse(""));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("DefinitionId", Codec.STRING, this.getDefinitionId());
    }
}