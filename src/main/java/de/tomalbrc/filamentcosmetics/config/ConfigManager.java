package de.tomalbrc.filamentcosmetics.config;

import com.mojang.brigadier.context.CommandContext;
import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tomalbrc.filamentcosmetics.FilamentCosmetics.id;

public class ConfigManager {
    public static final Path SERVER_COSMETICS_DIR = FabricLoader.getInstance().getConfigDir().resolve("FilamentCosmetics");

    private static final String TARGET_TEXTURE_PATH = "assets/filamentcosmetics/textures/";
    private static final String TARGET_MODEL_PATH = "assets/filamentcosmetics/models/item/";

    public record NavigationButton(Component name, Item baseItem, PolymerModelData polymerModelData, int slotIndex, List<String> lore) {}

    public static String configReloadPermission;
    public static String itemSkinsReloadPermission;
    public static String cosmeticsReloadPermission;

    private static Component successConfigReloadMessage;
    private static Component errorConfigReloadMessage;
    private static boolean legacyMode;

    public static final AbstractGuiConfig ITEM_SKINS_GUI_CONFIG = new ItemSkinsGUIConfig();
    public static final AbstractGuiConfig COSMETICS_GUI_CONFIG = new CosmeticsGUIConfig();

    public static void registerConfigs() {
        createAndLoadMainConfig();
        CustomItemRegistry.legacyMode = legacyMode;

        ITEM_SKINS_GUI_CONFIG.init();
        COSMETICS_GUI_CONFIG.init();
        CustomItemRegistry.initialize();

        registerResourcePackListener();
    }

    public static void registerResourcePackListener() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((builder) -> {
            Path resourcePackSourceDir = SERVER_COSMETICS_DIR.resolve("Assets");

            if (Files.isDirectory(resourcePackSourceDir)) {
                FilamentCosmetics.LOGGER.info("Scanning for .png and .json files in: {}", resourcePackSourceDir.toAbsolutePath());

                try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
                    pathStream
                            .filter(Files::isRegularFile)
                            .forEach(filePath -> {
                                Path fileNamePath = filePath.getFileName();
                                if (fileNamePath == null) {
                                    return;
                                }

                                String fileNameString = fileNamePath.toString();
                                String filenameLower = fileNameString.toLowerCase(Locale.ROOT);
                                String targetBaseDir = null;
                                byte[] data;

                                try {
                                    data = Files.readAllBytes(filePath);

                                    if (filenameLower.endsWith(".png")) {
                                        if(filenameLower.endsWith("_helmet.png") || filenameLower.endsWith("_chestplate.png") || filenameLower.endsWith("_leggings.png") || filenameLower.endsWith("_boots.png")){
                                            targetBaseDir = TARGET_TEXTURE_PATH + "item/armor/";
                                        } else if (filenameLower.endsWith("_layer_1.png") || filenameLower.endsWith("_layer_2.png")) {
                                            targetBaseDir = TARGET_TEXTURE_PATH + "models/armor/";
                                        } else {
                                            targetBaseDir = TARGET_TEXTURE_PATH + "item/";
                                        }
                                    } else if (filenameLower.endsWith(".json") || filenameLower.endsWith(".mcmeta")) {
                                        targetBaseDir = TARGET_MODEL_PATH;
                                        try {
                                            String content = new String(data, StandardCharsets.UTF_8);
                                            JSONObject jsonObject = new JSONObject(content);

                                            if (jsonObject.has("animation")) {
                                                targetBaseDir = TARGET_TEXTURE_PATH + "item/";
                                                FilamentCosmetics.LOGGER.debug("JSON file {} has 'animation' key, targeting TEXTURE_PATH.", fileNameString);
                                            }
                                        } catch (JSONException e) {
                                            FilamentCosmetics.LOGGER.warn("Could not parse JSON file {} to check for 'animation' key. Assuming it's a model. Error: {}", fileNameString, e.getMessage());
                                        }
                                    }

                                    if (targetBaseDir == null) {
                                        return;
                                    }

                                    String finalTargetPath = targetBaseDir + fileNameString;

                                    if (builder.addData(finalTargetPath, data)) {
                                        FilamentCosmetics.LOGGER.debug("Added {} -> {}", filePath.getFileName(), finalTargetPath);
                                    } else {
                                        FilamentCosmetics.LOGGER.warn("Could not add {} as {} to resource pack (maybe already exists?)", filePath.getFileName(), finalTargetPath);
                                    }
                                } catch (IOException e) {
                                    FilamentCosmetics.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
                                }
                            });
                    FilamentCosmetics.LOGGER.info("Finished adding custom .png and .json resources from {}", resourcePackSourceDir.toAbsolutePath());
                } catch (IOException e) {
                    FilamentCosmetics.LOGGER.error("Error walking directory {} for resource pack generation", resourcePackSourceDir.toAbsolutePath(), e);
                }
            } else {
                FilamentCosmetics.LOGGER.info("Custom resource source directory not found or is not a directory: {}. Skipping custom asset loading.", resourcePackSourceDir.toAbsolutePath());
                try {
                    Files.createDirectories(resourcePackSourceDir);
                    FilamentCosmetics.LOGGER.info("Created assets directory at: {}", resourcePackSourceDir.toAbsolutePath());
                } catch (IOException e) {
                    FilamentCosmetics.LOGGER.error("Failed to create assets directory: {}", resourcePackSourceDir.toAbsolutePath(), e);
                }
            }
        });
    }

    public static void loadDemoConfigs() {
        if(SERVER_COSMETICS_DIR.toFile().exists() && SERVER_COSMETICS_DIR.resolve("config.yml").toFile().exists()) {
            return;
        }
        try {
            Files.createDirectories(SERVER_COSMETICS_DIR);
        } catch (IOException e) {
            FilamentCosmetics.LOGGER.error("Failed to create base filamentcosmetics directory.", e);
        }


        Path demoConfigsPathSource = FabricLoader.getInstance().getModContainer("filamentcosmetics")
                .flatMap(modContainer -> modContainer.findPath("assets/filamentcosmetics/demo-configs/"))
                .orElse(null);

        if (demoConfigsPathSource == null) {
            FilamentCosmetics.LOGGER.warn("Could not find demo-configs path in mod assets.");
            return;
        }

        FilamentCosmetics.LOGGER.info("Loading demo configurations from {} to {}", demoConfigsPathSource, SERVER_COSMETICS_DIR);

        try (Stream<Path> stream = Files.walk(demoConfigsPathSource)) {
            stream.forEach(sourcePath -> {
                Path destPath = SERVER_COSMETICS_DIR.resolve(demoConfigsPathSource.relativize(sourcePath).toString());
                try {
                    if (Files.isDirectory(sourcePath)) {
                        if (Files.notExists(destPath)) {
                            Files.createDirectories(destPath);
                        }
                    } else {
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy demo file " + sourcePath + " to " + destPath, e);
                }
            });
        } catch (IOException | RuntimeException e) {
            FilamentCosmetics.LOGGER.error("Failed to load demo configs fully.", e);
        }
    }

    public static int reloadAllConfigsCommand(CommandContext<CommandSourceStack> context) {
        try {
            createAndLoadMainConfig();
            CustomItemRegistry.legacyMode = legacyMode;

            ITEM_SKINS_GUI_CONFIG.init();
            COSMETICS_GUI_CONFIG.init();
            CustomItemRegistry.reloadAll();

            context.getSource().sendSuccess(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendSuccess(() -> errorConfigReloadMessage, false);
            FilamentCosmetics.LOGGER.error("An error occurred during ALL configs reload!", e);
        }
        return 1;
    }
    public static int reloadItemSkinsConfigsCommand(CommandContext<CommandSourceStack> context) {
        try {
            createAndLoadMainConfig();
            CustomItemRegistry.legacyMode = legacyMode;

            ITEM_SKINS_GUI_CONFIG.init();
            CustomItemRegistry.reloadItemSkins();

            context.getSource().sendSuccess(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendSuccess(() -> errorConfigReloadMessage, false);
            FilamentCosmetics.LOGGER.error("An error occurred during ItemSkins configs reload!", e);
        }
        return 1;
    }

    public static int reloadCosmeticsConfigsCommand(CommandContext<CommandSourceStack> context) {
        try {
            createAndLoadMainConfig();
            CustomItemRegistry.legacyMode = legacyMode;

            COSMETICS_GUI_CONFIG.init();
            CustomItemRegistry.reloadCosmetics();

            context.getSource().sendSuccess(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendSuccess(() -> errorConfigReloadMessage, false);
            FilamentCosmetics.LOGGER.error("An error occurred during Cosmetics configs reload!", e);
        }
        return 1;
    }

    private static void createAndLoadMainConfig() {
        loadDemoConfigs();

        Path configFile = SERVER_COSMETICS_DIR.resolve("config.yml");
        YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

        try {
            yamlFile.createOrLoadWithComments();
            initializeMainConfigDefaults(yamlFile);
            yamlFile.loadWithComments();

            configReloadPermission = yamlFile.getString("permissions.reloadAllConfigs");
            itemSkinsReloadPermission = yamlFile.getString("permissions.reloadItemSkins");
            cosmeticsReloadPermission = yamlFile.getString("permissions.reloadCosmetics");
            successConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.success"));
            errorConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.error"));
            legacyMode = yamlFile.getBoolean("legacyMode");

        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load main configuration file (config.yml)", e);
        }
    }

    private static void initializeMainConfigDefaults(YamlFile yamlFile) {
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        yamlFile.options().headerFormatter()
                .prefixFirst("######################")
                .commentPrefix("## ")
                .commentSuffix(" ##")
                .suffixLast("######################");

        yamlFile.setHeader("Main Config File");

        yamlFile.addDefault("debug", false);
        yamlFile.addDefault("permissions.reloadAllConfigs", "filamentcosmetics.reload");
        yamlFile.addDefault("permissions.reloadItemSkins", "filamentcosmetics.reload.itemskins");
        yamlFile.addDefault("permissions.reloadCosmetics", "filamentcosmetics.reload.cosmetics");
        yamlFile.path("permissions").comment("If the mod cannot get permissions from config, the default one will be used");
        yamlFile.addDefault("configReload.message.success", "&aConfig successfully reload!");
        yamlFile.addDefault("configReload.message.error", "&cAn error occurred during configs reload!");
        yamlFile.path("legacyMode").addDefault(false).commentSide("If true, plugin will try to read some fields from older config structures for cosmetic/skin definitions. Recommended: false for new setups.");

        try {
            yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default main yml configuration", e);
        }
    }

    public static ItemStack createItemStack(String baseMaterialId, Component displayName, String cosmeticOrSkinId, List<Component> loreTexts) {
        Item baseItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(baseMaterialId));
        if (baseItem == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey()) && !baseMaterialId.equals(BuiltInRegistries.ITEM.getDefaultKey().toString())) {
            FilamentCosmetics.LOGGER.warn("Invalid baseMaterialId '{}' for item '{}'. Defaulting to minecraft:paper.", baseMaterialId, cosmeticOrSkinId);
            baseItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:paper")); // Fallback
        }

        PolymerModelData polymerModel;
        try {
            if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
                String modelIdPath = "item/armor/" + cosmeticOrSkinId + "_" + armorItem.getType().getName().toLowerCase();
                polymerModel = PolymerResourcePackUtils.requestModel(getItemFor(armorItem.getType()), id(modelIdPath));
            } else {
                polymerModel = PolymerResourcePackUtils.requestModel(baseItem, ResourceLocation.fromNamespaceAndPath(FilamentCosmetics.MOD_ID, "item/" + cosmeticOrSkinId));
            }
        } catch (Exception e) {
            FilamentCosmetics.LOGGER.error("Failed to request model for item id '{}' with base item '{}': {}", cosmeticOrSkinId, baseMaterialId, e.getMessage());
            ItemStack errorStack = new ItemStack(baseItem);
            errorStack.set(DataComponents.CUSTOM_NAME, Component.literal("Error: " + cosmeticOrSkinId));
            return errorStack;
        }


        ItemStack itemStack = new ItemStack(polymerModel.item());

        itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, comp -> comp.update(nbt -> {
            nbt.putString("cosmeticItemId", cosmeticOrSkinId);
        }));

        if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
            PolymerArmorModel armorModel = PolymerResourcePackUtils.requestArmor(id(cosmeticOrSkinId));
            itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(armorModel.color(), true));
        }

        if (loreTexts != null && !loreTexts.isEmpty()) {
            itemStack.set(DataComponents.LORE, new ItemLore(loreTexts));
        } else {
            itemStack.set(DataComponents.LORE, new ItemLore(Collections.emptyList()));
        }

        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(polymerModel.value()));
        itemStack.set(DataComponents.CUSTOM_NAME, displayName);

        return itemStack;
    }

    private static Item getItemFor(ArmorItem.Type type) {
        return switch (type) {
            case ArmorItem.Type.HELMET -> Items.LEATHER_HELMET;
            case ArmorItem.Type.CHESTPLATE -> Items.LEATHER_CHESTPLATE;
            case ArmorItem.Type.LEGGINGS -> Items.LEATHER_LEGGINGS;
            case ArmorItem.Type.BOOTS -> Items.LEATHER_BOOTS;
            default -> Items.STONE;
        };
    }

    public static List<Path> listFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            FilamentCosmetics.LOGGER.warn("Attempted to list files in a non-directory: {}", dir);
            return Collections.emptyList();
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            FilamentCosmetics.LOGGER.error("Failed to list files in directory: " + dir, e);
            return Collections.emptyList();
        }
    }
}