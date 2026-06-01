package dev.codexbat.vesper.plush;

import net.minecraft.util.Identifier;

/**
 * Immutable descriptor for one plushie type.
 * Build with PlushyDefinition.builder(id, entityTexture).boots().helmet().build()
 * then pass to PlushyRegistry.register().
 */
public final class PlushyDefinition {
    private final Identifier id;
    private final Identifier entityTexture; // e.g. "vesper:textures/entity/codex.png"
    private final boolean helmet, chestplate, leggings, boots, mainhand, offhand;

    private PlushyDefinition(Builder b) {
        id = b.id; entityTexture = b.tex;
        helmet = b.helmet; chestplate = b.chestplate;
        leggings = b.leggings; boots = b.boots;
        mainhand = b.mainhand; offhand = b.offhand;
    }

    public Identifier getId()            { return id; }
    public Identifier getEntityTexture() { return entityTexture; }
    public boolean canWearHelmet()       { return helmet; }
    public boolean canWearChestplate()   { return chestplate; }
    public boolean canWearLeggings()     { return leggings; }
    public boolean canWearBoots()        { return boots; }
    public boolean canHoldMainhand()     { return mainhand; }
    public boolean canHoldOffhand()      { return offhand; }

    public static Builder builder(Identifier id, Identifier entityTexture) {
        return new Builder(id, entityTexture);
    }

    public static final class Builder {
        private final Identifier id, tex;
        private boolean helmet, chestplate, leggings, boots, mainhand, offhand;
        Builder(Identifier id, Identifier tex) { this.id = id; this.tex = tex; }

        public Builder helmet()       { helmet = true;       return this; }
        public Builder chestplate()   { chestplate = true;   return this; }
        public Builder leggings()     { leggings = true;     return this; }
        public Builder boots()        { boots = true;        return this; }
        public Builder mainhand()     { mainhand = true;     return this; }
        public Builder offhand()      { offhand = true;      return this; }
        public Builder fullEquipment(){ return helmet().chestplate().leggings().boots().mainhand().offhand(); }

        public PlushyDefinition build() { return new PlushyDefinition(this); }
    }
}