package de.tomalbrc.filamentcosmetics.config;

import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public abstract class AbstractGuiConfig {

    protected final Path configFilePath;
    protected YamlFile yamlFile;

    public String guiNameString;
    public int[] displaySlots;
    public String permissionOpenGui;
    public String messageUnlockedString;
    public String messageLockedString;
    public boolean pageIndicatorEnabled;
    public boolean replaceInventory;
    public MenuType<ChestMenu> screenHandlerType;

    public final Map<String, ConfigManager.NavigationButton> navigationButtons = new HashMap<>();

    public AbstractGuiConfig(String configFileName) {
        this.configFilePath = ConfigManager.SERVER_COSMETICS_DIR.resolve(configFileName);
    }

    public void init() {
        this.yamlFile = new YamlFile(configFilePath.toAbsolutePath().toString());
        try {
            yamlFile.createOrLoadWithComments();
            setupDefaultConfig(yamlFile);
            yamlFile.loadWithComments();

            loadCommonConfig(yamlFile);
            loadSpecificConfig(yamlFile);
            loadAllNavigationButtons(yamlFile);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load " + configFilePath.getFileName().toString(), e);
        }
    }

    private void setupDefaultConfig(YamlFile file) {
        file.setCommentFormat(YamlCommentFormat.PRETTY);
        file.options().headerFormatter()
                .prefixFirst("###############################")
                .commentPrefix("## ")
                .commentSuffix(" ##")
                .suffixLast("###############################");
        file.setHeader(getGuiConfigHeader());

        addCommonDefaults(file);
        addSpecificDefaults(file);

        ConfigurationSection buttonsSection = file.getConfigurationSection("buttons");
        if (buttonsSection == null) {
            buttonsSection = file.createSection("buttons");
        }
        addDefaultButtons(buttonsSection);

        try {
            file.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default yml configuration for " + file.getFilePath(), e);
        }
    }

    private void loadCommonConfig(YamlFile file) {
        this.guiNameString = file.getString("guiName");
        this.displaySlots = file.getIntegerList("displaySlots").stream().mapToInt(Integer::intValue).toArray();
        this.permissionOpenGui = file.getString("permissions.openGui");
        this.messageUnlockedString = file.getString("messages.unlocked");
        this.messageLockedString = file.getString("messages.locked");
        this.pageIndicatorEnabled = file.getBoolean("pageIndicatorEnabled", false);
        this.replaceInventory = file.getBoolean("replaceInventory");
    }

    protected void addCommonDefaults(YamlFile file) {
        file.addDefault("guiName", "Default GUI Name");
        file.addDefault("displaySlots", List.of(19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43));
        file.addDefault("permissions.openGui", "filamentcosmetics.gui.default");
        file.addDefault("messages.unlocked", "&a(Unlocked)");
        file.addDefault("messages.locked", "&c(Locked)");
        file.addDefault("pageIndicatorEnabled", false);
        file.addDefault("replaceInventory", false);
        loadGuiSize(file.getInt("guiRows", 6));
    }

    private void loadGuiSize(int guiRows) {
        if (guiRows < 1 || guiRows > 6) {
            FilamentCosmetics.LOGGER.warn("Invalid guiRows value '{}' in {}. Must be between 1 and 6. Defaulting to 6.", guiRows, this.configFilePath.getFileName());
            guiRows = 6;
        }
        this.screenHandlerType = switch (guiRows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    protected void loadNavigationButton(YamlFile yamlFile, String buttonKey) {
        String basePath = "buttons." + buttonKey;
        if (!yamlFile.isConfigurationSection(basePath)) {
            FilamentCosmetics.LOGGER.warn("Button configuration for '{}' not found in {}.", buttonKey, this.configFilePath.getFileName());
            return;
        }
        String baseItemString = yamlFile.getString(basePath + ".item");
        if (baseItemString == null || baseItemString.isEmpty()) {
            FilamentCosmetics.LOGGER.error("Button '{}' in {} is missing 'item' field.", buttonKey, this.configFilePath.getFileName());
            return;
        }
        String complitedItemString = baseItemString.contains(":") ? baseItemString : "minecraft:" + baseItemString.toLowerCase();

        Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(complitedItemString));
        if (item == BuiltInRegistries.ITEM.getValue(BuiltInRegistries.ITEM.getDefaultKey()) && !complitedItemString.equals(BuiltInRegistries.ITEM.getDefaultKey().toString())) {
            FilamentCosmetics.LOGGER.error("Button '{}' in {} has invalid item id: {}. Defaulting to paper.", buttonKey, this.configFilePath.getFileName(), complitedItemString);
            item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse("minecraft:paper"));
            complitedItemString = "minecraft:paper";
        }

        ResourceLocation polymerModelData = null;
        if (yamlFile.isSet(basePath + ".textureName")) {
            String textureName = yamlFile.getString(basePath + ".textureName");
            if (textureName != null && !textureName.isEmpty()) {
                try {
                    polymerModelData = ResourceLocation.fromNamespaceAndPath(FilamentCosmetics.MOD_ID, "item/" + textureName);
                } catch (Exception e) {
                    FilamentCosmetics.LOGGER.error("Failed to request model for button '{}' (item: {}, texture: {}): {}", buttonKey, complitedItemString, textureName, e.getMessage());
                }
            }
        } else {
//            filamentcosmetics.LOGGER.error("Texture name for '{}' is undefined!", buttonKey);
        }

        List<String> loreStrings = yamlFile.getStringList(basePath + ".lore");

        navigationButtons.put(buttonKey, new ConfigManager.NavigationButton(
                Utils.formatDisplayName(yamlFile.getString(basePath + ".name", "Button " + buttonKey)),
                item,
                polymerModelData,
                yamlFile.getInt(basePath + ".slotIndex"),
                loreStrings
        ));
    }

    protected void addDefaultButtonToSection(ConfigurationSection buttonsSection, String buttonName, Map<String, Object> properties) {
        ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(buttonName);
        if (buttonSection == null) {
            buttonSection = buttonsSection.createSection(buttonName);
        }
        final ConfigurationSection finalButtonSection = buttonSection;
        properties.forEach((key, value) -> {
            if (!finalButtonSection.contains(key)) {
                finalButtonSection.set(key, value);
            }
        });
    }

    public Component getGuiName() {
        return Utils.formatDisplayName(this.guiNameString);
    }

    public Component getMessageUnlocked() {
        return Utils.formatDisplayName(this.messageUnlockedString);
    }

    public Component getMessageLocked() {
        return Utils.formatDisplayName(this.messageLockedString);
    }

    public ConfigManager.NavigationButton getButtonConfig(String buttonKey) {
        ConfigManager.NavigationButton button = navigationButtons.get(buttonKey);
        if (button == null) {
            FilamentCosmetics.LOGGER.warn("Requested non-existent button config: '{}' from {}", buttonKey, this.configFilePath.getFileName());
            return new ConfigManager.NavigationButton(Component.literal("Error"), BuiltInRegistries.ITEM.getValue(ResourceLocation.parse("minecraft:barrier")), null, 0, Collections.emptyList());
        }
        return button;
    }

    // --- Abstract methods for subclasses ---
    protected abstract String getGuiConfigHeader();
    protected abstract void addSpecificDefaults(YamlFile file);
    protected abstract void loadSpecificConfig(YamlFile file);
    protected abstract void addDefaultButtons(ConfigurationSection buttonsSection);
    protected abstract void loadAllNavigationButtons(YamlFile file);
}
