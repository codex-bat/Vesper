package dev.codexbat.vesper.client;

import dev.codexbat.vesper.Vesper;
import dev.codexbat.vesper.api.glint.*;
import dev.codexbat.vesper.api.glint.type.AmbientGlint;
import dev.codexbat.vesper.api.glint.type.SparkleGlint;
import dev.codexbat.vesper.api.glint.type.SpecialGlint;
import dev.codexbat.vesper.api.glint.type.StandardGlint;
import dev.codexbat.vesper.client.bootstrap.ClientBootstrap;
import dev.codexbat.vesper.client.glint.internal.render.ItemPositionTracker;
import dev.codexbat.vesper.client.glint.internal.render.WorldGlintParticleManager;
import dev.codexbat.vesper.client.particle.VesperParticleTypes;
import dev.codexbat.vesper.client.plush.render.PlushyBlockEntityRenderer;
import dev.codexbat.vesper.plush.PlushyRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class VesperClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		Vesper.LOGGER.info("Client initialized.");

		//Le Glints
		ClientBootstrap.initialize();
	}
}