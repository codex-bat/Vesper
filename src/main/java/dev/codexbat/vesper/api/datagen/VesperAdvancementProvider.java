package dev.codexbat.vesper.api.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Abstract base class for advancement providers. Extend this and implement
 * {@link #generate(Consumer, RegistryWrapper.WrapperLookup)}.
 *
 * <p>Features a fluent {@link AdvancementBuilder} accessible via
 * {@link #advancement(Identifier)}, and a helper
 * for referencing vanilla advancements as parents.
 *
 * <p><b>Usage:</b>
 * <pre>
 * public class MyAdvancementProvider extends VesperAdvancementProvider {
 *     public MyAdvancementProvider(FabricDataOutput out, CompletableFuture&lt;...&gt; reg) {
 *         super(out, reg);
 *     }
 *
 *     {@literal @}Override
 *     protected void generate(Consumer&lt;AdvancementEntry&gt; exporter, RegistryWrapper.WrapperLookup lookup) {
 *         AdvancementEntry root = advancement(id("root"))
 *             .title("My Mod Advancements")
 *             .description("Where it all begins")
 *             .icon(Items.DIAMOND)
 *             .background(Identifier.of("textures/block/stone.png"))
 *             .criterion("tick", VesperCriteria.tick())
 *             .secret()
 *             .build(exporter);
 *
 *         advancement(id("get_diamond"))
 *             .parent(root)
 *             .title("Shiny!")
 *             .description("Pick up a diamond")
 *             .icon(Items.DIAMOND)
 *             .task()
 *             .criterion("diamond", VesperCriteria.obtainItem(Items.DIAMOND))
 *             .build(exporter);
 *     }
 * }
 * </pre>
 */
public abstract class VesperAdvancementProvider extends FabricAdvancementProvider {

    protected VesperAdvancementProvider(FabricDataOutput output,
                                        CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
        super(output, lookup);
    }

    @Override
    public final void generateAdvancement(RegistryWrapper.WrapperLookup lookup,
                                          Consumer<AdvancementEntry> exporter) {
        generate(exporter, lookup);
    }

    /**
     * Implement this to declare your advancements.
     *
     * @param exporter pass to {@link AdvancementBuilder#build} to write each advancement
     * @param lookup   registry access; use {@link #vanilla(String, RegistryWrapper.WrapperLookup)}
     *                 to reference vanilla advancements as parents
     */
    protected abstract void generate(Consumer<AdvancementEntry> exporter,
                                     RegistryWrapper.WrapperLookup lookup);

    // ── Helpers available inside generate() ──────────────────────────────────

    /** Start building an advancement with the given id. */
    protected static AdvancementBuilder advancement(Identifier id) {
        return new AdvancementBuilder(id);
    }

    /**
     * Look up a vanilla advancement entry so you can use it as a parent.
     *
     * <pre>
     * .parent(vanilla("story/root", lookup))
     * .parent(vanilla("nether/root", lookup))
     * </pre>
     */
    protected static RegistryEntry.Reference<Advancement> vanilla(String path, RegistryWrapper.WrapperLookup lookup) {
        return lookup
                .getOrThrow(RegistryKeys.ADVANCEMENT)
                .getOrThrow(RegistryKey.of(RegistryKeys.ADVANCEMENT, Identifier.of("minecraft", path)));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Fluent AdvancementBuilder
    // ═════════════════════════════════════════════════════════════════════════

    public static final class AdvancementBuilder {

        private final Identifier id;
        private final Advancement.Builder vanilla = Advancement.Builder.create();

        // Display fields
        private Text title;
        private Text description;
        private ItemStack icon;
        private Identifier background;       // only relevant for root advancements
        private AdvancementFrame frame = AdvancementFrame.TASK;
        private boolean showToast = true;
        private boolean announceToChat = true;
        private boolean hidden = false;

        private AdvancementBuilder(Identifier id) {
            this.id = id;
        }

        // ── Parent ─────────────────────────────────────────────────────────

        /** Chain off a previously built advancement. */
        public AdvancementBuilder parent(AdvancementEntry parent) {
            vanilla.parent(parent);
            return this;
        }

        /**
         * Reference a parent by its full string identifier, e.g.
         * {@code "minecraft:story/root"} — useful if you don't have the
         * AdvancementEntry object at hand.
         */
        @SuppressWarnings("deprecation")
        public AdvancementBuilder parent(Identifier parentId) {
            vanilla.parent(parentId);
            return this;
        }

        // ── Display ────────────────────────────────────────────────────────

        public AdvancementBuilder title(Text text) {
            this.title = text;
            return this;
        }

        /** Uses a translation key (preferred for localisable mods). */
        public AdvancementBuilder title(String key) {
            return title(Text.translatable(key));
        }

        /** Literal string title — handy for quick prototyping. */
        public AdvancementBuilder titleLiteral(String literal) {
            return title(Text.literal(literal));
        }

        public AdvancementBuilder description(Text text) {
            this.description = text;
            return this;
        }

        public AdvancementBuilder description(String key) {
            return description(Text.translatable(key));
        }

        public AdvancementBuilder descriptionLiteral(String literal) {
            return description(Text.literal(literal));
        }

        public AdvancementBuilder icon(ItemConvertible item) {
            this.icon = new ItemStack(item);
            return this;
        }

        public AdvancementBuilder icon(ItemStack stack) {
            this.icon = stack.copy();
            return this;
        }

        /**
         * Background texture. Only has visible effect on root advancements
         * (i.e. advancements with no parent).
         */
        public AdvancementBuilder background(Identifier textureId) {
            this.background = textureId;
            return this;
        }

        // ── Frame / shape ──────────────────────────────────────────────────

        /** Plain square frame (default). */
        public AdvancementBuilder task() {
            this.frame = AdvancementFrame.TASK;
            return this;
        }

        /** Oval frame — for mid-tier goals. */
        public AdvancementBuilder goal() {
            this.frame = AdvancementFrame.GOAL;
            return this;
        }

        /** Star-shaped frame, purple tint, loud fanfare. */
        public AdvancementBuilder challenge() {
            this.frame = AdvancementFrame.CHALLENGE;
            return this;
        }

        public AdvancementBuilder frame(AdvancementFrame frame) {
            this.frame = frame;
            return this;
        }

        // ── Visibility ─────────────────────────────────────────────────────

        /** Hide from the advancement screen until earned. */
        public AdvancementBuilder hidden() {
            this.hidden = true;
            return this;
        }

        /** Suppress the on-screen toast notification. */
        public AdvancementBuilder noToast() {
            this.showToast = false;
            return this;
        }

        /** Suppress the chat message on completion. */
        public AdvancementBuilder silent() {
            this.announceToChat = false;
            return this;
        }

        /** hidden + noToast + silent — for internal/trigger-only advancements. */
        public AdvancementBuilder secret() {
            return hidden().noToast().silent();
        }

        // ── Criteria ───────────────────────────────────────────────────────

        /**
         * Add a named criterion. Use {@link VesperCriteria} for convenient
         * factories covering every vanilla criterion type.
         */
        public <T extends CriterionConditions> AdvancementBuilder criterion(
                String name, AdvancementCriterion<T> criterion) {
            vanilla.criterion(name, criterion);
            return this;
        }

        // ── Requirements (AND / OR grouping) ──────────────────────────────

        // To use OR groups you must build the AdvancementRequirements yourself
        // and set it via the dedicated method. Example:
        //
        // AdvancementRequirements req = AdvancementRequirements.anyOf(Set.of("sword", "axe"));
        // builder.requirements(req);
        //
        // The vanilla builder does not expose a method to add individual OR groups,
        // so this builder does not include a requireAny shortcut.

        public AdvancementBuilder requirements(AdvancementRequirements requirements) {
            vanilla.requirements(requirements);
            return this;
        }

        // ── Rewards ────────────────────────────────────────────────────────

        public AdvancementBuilder rewardXp(int amount) {
            vanilla.rewards(AdvancementRewards.Builder.experience(amount));
            return this;
        }

        /**
         * Add a recipe reward.
         * @param recipeId e.g. {@code Identifier.of("minecraft", "some_recipe")}
         */
        public AdvancementBuilder rewardRecipe(Identifier recipeId) {
            RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);
            vanilla.rewards(AdvancementRewards.Builder.recipe(recipeKey));
            return this;
        }

        public AdvancementBuilder rewards(AdvancementRewards rewards) {
            vanilla.rewards(rewards);
            return this;
        }

        // ── Build ──────────────────────────────────────────────────────────

        /**
         * Applies the display, writes the advancement to {@code exporter},
         * and returns the resulting {@link AdvancementEntry} so it can be
         * passed to child advancements via {@link #parent(AdvancementEntry)}.
         */
        public AdvancementEntry build(Consumer<AdvancementEntry> exporter) {
            if (title != null && description != null && icon != null) {
                vanilla.display(new AdvancementDisplay(
                        icon,
                        title,
                        description,
                        Optional.ofNullable(background)
                                .map(AssetInfo.TextureAssetInfo::new),
                        frame,
                        showToast,
                        announceToChat,
                        hidden
                ));
            }
            return vanilla.build(exporter, id.toString());
        }
    }
}