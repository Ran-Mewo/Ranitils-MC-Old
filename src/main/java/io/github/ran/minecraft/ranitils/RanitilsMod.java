package io.github.ran.minecraft.ranitils;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.util.AnyArmor;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RanitilsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Ranitils");

	@Override
	public void onInitialize() {
		LOGGER.info("Hewwo!");
		ModConfig.init();
		AnyArmor.registerCommand();
	}
}
