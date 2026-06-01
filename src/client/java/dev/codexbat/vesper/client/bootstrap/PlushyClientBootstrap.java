package dev.codexbat.vesper.client.bootstrap;

import dev.codexbat.vesper.client.glint.internal.render.ItemPositionTracker;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleManager;
import dev.codexbat.vesper.client.plush.render.PlushyBlockEntityRenderer;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Everything plushies is here :3
 */
public final class PlushyClientBootstrap {
    private PlushyClientBootstrap() {}

    public static void initialize() {
        // Le Modelle
        //PlushyModelRegistry.register(Vesper.id("codex_plush"), CodexPlushModel.MODEL);

    }
}