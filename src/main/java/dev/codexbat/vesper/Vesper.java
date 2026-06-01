package dev.codexbat.vesper;

import dev.codexbat.vesper.api.recipe.ComponentIngredient;
import dev.codexbat.vesper.bootstrap.VesperBootstrap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vesper implements ModInitializer {
	public static final String MOD_ID = "vesper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Vesper library is initializing (common)");
		// Register networking packets (common side definition)
		//VesperNetworking.register();

		// Config & syncing setup
		//VesperConfig.init();

		// New recipes
		CustomIngredientSerializer.register(ComponentIngredient.SERIALIZER);

		// All mod-content
		VesperBootstrap.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}