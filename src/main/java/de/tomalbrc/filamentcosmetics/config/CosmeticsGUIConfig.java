package de.tomalbrc.filamentcosmetics.config;

import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.gui.resources.GuiTextures;
import de.tomalbrc.filamentcosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;

public class CosmeticsGUIConfig extends AbstractGuiConfig {
    public static int[] colorSlots;
    public static int[] colorGradientSlots;
    public static int colorInputSlot;
    public static int colorOutputSlot;
    public static String[] colorHexValues;
    public static String colorPickerGUINameString;
    public static float saturationAdjustmentValue;
    public static String signType;
    public static PolymerModelData paintItemPolymerModelData;
    public static DyeColor signColor;
    public static String successMessageString;
    public static String errorMessageString;

    private static List<String> textLines;

    public CosmeticsGUIConfig() {
        super("CosmeticsGUI.yml");
    }

    @Override
    protected String getGuiConfigHeader() {
        return "Cosmetics GUI Config File";
    }

    @Override
    protected void addSpecificDefaults(YamlFile file) {
        file.addDefault("slots.colorInput", 28);
        file.addDefault("slots.colorOutput", 34);
        file.addDefault("paintItemModelPath", "paint_button");
        file.addDefault("slots.color", new int[]{21, 22, 23, 30, 31, 32, 39, 40, 41});
        file.addDefault("slots.colorGradient", new int[]{1, 2, 3, 4, 5, 6, 7});
        file.addDefault("colorPicker.hexValues", new String[]{"ff0000", "ff7700", "ffff00", "ff0099", "ffffff", "09ff00", "8800ff", "0000ff", "00ffff"});
        file.addDefault("colorPicker.name", "Color Picker");
        file.addDefault("colorPicker.saturationAdjustmentValue", 20.0F);
        file.addDefault("permissions.openGui", "filamentcosmetics.gui.cosmetics");
        file.addDefault("colorInput.signType", "minecraft:acacia_wall_sign");
        file.addDefault("colorInput.signColor", "WHITE");
        file.addDefault("colorInput.textLines", List.of("Enter the color in", "HEX format", "Example: #FFFFFF"));
        file.addDefault("colorInput.messages.success", "&aColor successfully changed!");
        file.addDefault("colorInput.messages.error", "&cIncorrect color format!");


        if (!file.contains("displaySlots")) {
            file.set("displaySlots", List.of(
                    19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43
            ));
        }
    }

    @Override
    protected void loadSpecificConfig(YamlFile file) {
        colorSlots = file.getIntegerList("slots.color").stream().mapToInt(Integer::intValue).toArray();
        colorGradientSlots = file.getIntegerList("slots.colorGradient").stream().mapToInt(Integer::intValue).toArray();
        colorInputSlot = file.getInt("slots.colorInput");
        colorOutputSlot = file.getInt("slots.colorOutput");
        colorHexValues = file.getStringList("colorPicker.hexValues").toArray(new String[0]);
        colorPickerGUINameString = file.getString("colorPicker.name");
        saturationAdjustmentValue = (float) file.getDouble("colorPicker.saturationAdjustmentValue", 20.0);


        String paintItemModelPath = file.getString("paintItemModelPath");
        if (paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
            try {
                paintItemPolymerModelData = PolymerResourcePackUtils.requestModel(Items.LEATHER_HORSE_ARMOR, ResourceLocation.fromNamespaceAndPath(FilamentCosmetics.MOD_ID, "item/" + paintItemModelPath));
            } catch (Exception e) {
                FilamentCosmetics.LOGGER.error("Failed to load paintItemModelData for path '{}': {}", paintItemModelPath, e.getMessage());
                paintItemPolymerModelData = null;
            }
        } else {
            paintItemPolymerModelData = null;
        }


        signType = file.getString("colorInput.signType");
        try {
            signColor = DyeColor.valueOf(file.getString("colorInput.signColor", "WHITE").toUpperCase());
        } catch (IllegalArgumentException e) {
            FilamentCosmetics.LOGGER.warn("Invalid signColor '{}' in CosmeticsGUI.yml, defaulting to WHITE.", file.getString("colorInput.signColor"));
            signColor = DyeColor.WHITE;
        }
        textLines = file.getStringList("colorInput.textLines");
        successMessageString = file.getString("colorInput.messages.success");
        errorMessageString = file.getString("colorInput.messages.error");
    }

    @Override
    protected void addDefaultButtons(ConfigurationSection buttonsSection) {
        Map<String, Map<String, Object>> buttonDefaults = new java.util.HashMap<>();
        buttonDefaults.put("next", Map.of(
                "name", "Next", "item", "minecraft:paper", "textureName", "next", "slotIndex", 51));
        buttonDefaults.put("previous", Map.of(
                "name", "Back", "item", "minecraft:paper", "textureName", "previous", "slotIndex", 47));
        buttonDefaults.put("removeSkin", Map.of(
                "name", "Remove cosmetic", "item", "minecraft:paper", "textureName", "remove", "slotIndex", 49));
        buttonDefaults.put("toggleColorView", Map.of(
                "name", "Toggle view", "item", "minecraft:diamond_chestplate", "slotIndex", 10));
        buttonDefaults.put("enterColor", Map.of(
                "name", "Enter custom color", "item", "minecraft:oak_sign", "slotIndex", 9,
                "lore", List.of("§eEnter the color in HEX format", "§ein the first line of the sign")));
        buttonDefaults.put("decreaseBrightness", Map.of(
                "name", "Decrease brightness", "item", "minecraft:paper", "textureName", "previous", "slotIndex", 15));
        buttonDefaults.put("increaseBrightness", Map.of(
                "name", "Increase brightness", "item", "minecraft:paper", "textureName", "next", "slotIndex", 16));

        buttonDefaults.put("filter.show-all-skins", Map.of(
                "name", "<blue>Cosmetic Filter", "item", "minecraft:diamond_chestplate", "slotIndex", 10,
                "lore", List.of("&aAll cosmetics &7(Selected)", "&7Available cosmetics", "", "&aClick to change mode!")));
        buttonDefaults.put("filter.show-owned-skins", Map.of(
                "name", "<blue>Cosmetic Filter", "item", "minecraft:golden_chestplate", "slotIndex", 10,
                "lore", List.of("&7All cosmetics", "&aAvailable cosmetics &7(Selected)", "", "&aClick to change mode!")));
        
        buttonDefaults.put("filter.hats-disabled", Map.of(
                "name", "<blue>Hats", "item", "minecraft:leather_helmet", "slotIndex", 13,
                "lore", List.of()));
        buttonDefaults.put("filter.hats-enabled", Map.of(
                "name", "<blue>Hats", "item", "minecraft:diamond_helmet", "slotIndex", 13,
                "lore", List.of()));
        
        buttonDefaults.put("filter.body-cosmetics-disabled", Map.of(
                "name", "<blue>Body Cosmetics", "item", "minecraft:leather_chestplate", "slotIndex", 15,
                "lore", List.of()));
        buttonDefaults.put("filter.body-cosmetics-enabled", Map.of(
                "name", "<blue>Body Cosmetics", "item", "minecraft:diamond_chestplate", "slotIndex", 15,
                "lore", List.of()));

        buttonDefaults.put("pageIndicator", Map.of(
                "name", "Page", "item", "minecraft:paper", "slotIndex", 53));

        buttonDefaults.forEach((buttonName, properties) -> addDefaultButtonToSection(buttonsSection, buttonName, properties));
    }

    @Override
    protected void loadAllNavigationButtons(YamlFile file) {
        loadNavigationButton(file, "next");
        loadNavigationButton(file, "previous");
        loadNavigationButton(file, "removeSkin");
        loadNavigationButton(file, "toggleColorView");
        loadNavigationButton(file, "enterColor");
        loadNavigationButton(file, "decreaseBrightness");
        loadNavigationButton(file, "increaseBrightness");

        loadNavigationButton(file, "filter.show-all-skins");
        loadNavigationButton(file, "filter.show-owned-skins");

        loadNavigationButton(file, "filter.hats-disabled");
        loadNavigationButton(file, "filter.hats-enabled");

        loadNavigationButton(file, "filter.body-cosmetics-disabled");
        loadNavigationButton(file, "filter.body-cosmetics-enabled");

        loadNavigationButton(file, "pageIndicator");
    }

    public static List<String> getTextLines() { // For sign
        return new ArrayList<>(textLines); // Return a copy
    }

    public static Component getSuccessColorChangeMessage() {
        return Utils.formatDisplayName(successMessageString);
    }

    public static Component getErrorColorChangeMessage() {
        return Utils.formatDisplayName(errorMessageString);
    }

    public static Component getColorPickerGUIName() {
        return Utils.formatDisplayName(colorPickerGUINameString);
    }

    public Component getGuiName() {
        return GuiTextures.COSMETICS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
    }

}
