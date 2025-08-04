package de.tomalbrc.filamentcosmetics;

import de.tomalbrc.filamentcosmetics.command.CosmeticCommands;
import de.tomalbrc.filamentcosmetics.config.ConfigManager;
import de.tomalbrc.filamentcosmetics.database.DatabaseManager;
import de.tomalbrc.filamentcosmetics.gui.resources.GuiTextures;
import de.tomalbrc.filamentcosmetics.gui.resources.UiResourceCreator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilamentCosmetics implements ModInitializer {
	public static final String MOD_ID = "filamentcosmetics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ConfigManager.registerConfigs();
		DatabaseManager.init();

		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		CosmeticCommands.registerCommands();
		UiResourceCreator.setup();
		GuiTextures.register();

		if (PolymerResourcePackUtils.addModAssets(MOD_ID)) {
			LOGGER.info("Successfully added mod assets for " + MOD_ID);
		} else {
			LOGGER.error("Failed to add mod assets for " + MOD_ID);
		}
		PolymerResourcePackUtils.markAsRequired();
	}


	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	private void onServerStarting(MinecraftServer server) {
		SERVER = server;
	}
}