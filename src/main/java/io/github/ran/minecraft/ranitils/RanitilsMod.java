package io.github.ran.minecraft.ranitils;

import io.github.ran.minecraft.ranitils.config.ModConfig;
import io.github.ran.minecraft.ranitils.util.AnyArmor;
import io.github.ran.minecraft.ranitils.util.VerticalTP;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RanitilsMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Ranitils");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hewwo!");
		ModConfig.init();

		AnyArmor.register();
		VerticalTP.register();
	}
}