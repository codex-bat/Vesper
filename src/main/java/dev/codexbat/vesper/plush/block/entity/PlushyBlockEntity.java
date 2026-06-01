package dev.codexbat.vesper.plush.block.entity;

import dev.codexbat.vesper.plush.PlushyRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jspecify.annotations.Nullable;

public class PlushyBlockEntity extends BlockEntity {

    public static final String DEFINITION_KEY = "PlushyDefinition";
    private Identifier definitionId;   // null until set

    private static final int SQUISH_DURATION = 6;

    private int     squishTicks = 0;
    private boolean entityOnTop = false;  // server-side only, not synced

    public PlushyBlockEntity(BlockPos pos, BlockState state) {
        super(PlushyRegistry.PLUSHY_BLOCK_ENTITY, pos, state);
    }

    /** Called from PlushyItem when the block is placed */
    public void setDefinitionId(Identifier id) {
        this.definitionId = id;
        markDirty();
        notifyListeners();
    }

    public Identifier getDefinitionId() {
        return definitionId;
    }

    // Public API

    /**
     * Triggers the short tap-squish animation.
     *
     * @return {@code true} if the squish was applied;
     *         {@code false} if blocked because an entity is currently standing on top.
     */
    public boolean squish() {
        if (entityOnTop) return false;
        squishTicks = SQUISH_DURATION;
        markDirty();
        notifyListeners();
        return true;
    }

    /**
     * Interpolated squish value in [0, 1].
     * Fades from 1 to 0 over SQUISH_DURATION ticks after each squish trigger.
     */
    public float getSquish(float tickDelta) {
        float t = Math.max(0.0f, squishTicks - tickDelta);
        return t / (float) SQUISH_DURATION;
    }

    // Tick

    public void tick() {
        if (world == null) return;

        if (!world.isClient()) {
            boolean onTop = checkEntityOnTop();
            if (entityOnTop && !onTop) {
                // Entity just stepped off - trigger the spring-back animation.
                squishTicks = SQUISH_DURATION;
                markDirty();
                notifyListeners();
            }
            entityOnTop = onTop;
        }

        if (squishTicks > 0) {
            squishTicks--;
        }
    }

    // Internals

    /**
     * Returns true when a living entity's feet sit within the narrow tolerance
     * band at the top surface of the collision shape (Y + 12.5/16 ~= Y + 0.781).
     *
     * <p>Entities standing beside the block are at Y + 0, so their
     * foot Y differs from topY by ~0.78 - well outside the 0.1 tolerance.
     */
    private boolean checkEntityOnTop() {
        if (world == null || world.isClient()) return false;

        double topY   = pos.getY() + 12.5 / 16.0;
        Box    region = new Box(
                pos.getX(),       topY - 0.05, pos.getZ(),
                pos.getX() + 1.0, topY + 2.0,  pos.getZ() + 1.0
        );

        return !world.getEntitiesByClass(
                LivingEntity.class,
                region,
                entity -> Math.abs(entity.getY() - topY) <= 0.1
        ).isEmpty();
    }

    private void notifyListeners() {
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    // Serialisation

    @Override
    protected void readData(ReadView view) {
        squishTicks = view.getInt("SquishTicks", 0);
        String def = view.getString(DEFINITION_KEY, null);
        this.definitionId = def == null ? null : Identifier.of(def);
    }

    @Override
    protected void writeData(WriteView view) {
        view.putInt("SquishTicks", squishTicks);
        if (definitionId != null) {
            view.putString(DEFINITION_KEY, definitionId.toString());
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = createNbt(registries);
        if (definitionId != null) {
            nbt.putString(DEFINITION_KEY, definitionId.toString());
        }
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}