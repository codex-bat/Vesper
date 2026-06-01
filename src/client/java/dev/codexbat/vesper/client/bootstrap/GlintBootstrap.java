package dev.codexbat.vesper.client.bootstrap;

import dev.codexbat.vesper.client.glint.internal.render.ItemPositionTracker;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleManager;
import dev.codexbat.vesper.client.plush.render.PlushyBlockEntityRenderer;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;

public class GlintBootstrap {
    private GlintBootstrap() {}

    public static void initialize() {
        // Le GlintManagerre
        WorldGlintParticleManager.registerFactory();

        // Le... I don't remember what this is
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof PlayerEntity player) {
                ItemPositionTracker.evict(player.getUuid());
            }
        });

        //Le Rendererre --- YOU CAN'T JUST PUT 'LE' IN FRONT OF EVERY WORD AND FRANCHISE (franchise?) IT!!! you sure? watch me :3
        BlockEntityRendererFactories.register(
                PlushyRegistry.PLUSHY_BLOCK_ENTITY,
                PlushyBlockEntityRenderer::new
        );
    }
}
