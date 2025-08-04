package de.tomalbrc.filamentcosmetics.gui;

import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.config.CosmeticsGUIConfig;
import de.tomalbrc.filamentcosmetics.gui.resources.GuiTextures;
import de.tomalbrc.filamentcosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;

import static de.tomalbrc.filamentcosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;

// TODO: remove mutable variables
public class ColorPickerComponent {

    private final ServerPlayer player;
    private final ItemStack itemToColor;
    private final Consumer<ItemStack> onColorSelectCallback;

    public ColorPickerComponent(ServerPlayer player, ItemStack itemToColor, Consumer<ItemStack> onColorSelectCallback) {
        this.player = player;
        this.itemToColor = itemToColor.copy();
        this.onColorSelectCallback = onColorSelectCallback;
    }

    public void open() {
        try {
            new ColorPickerScreen(player, itemToColor, onColorSelectCallback).open();
        } catch (Exception e) {
            player.displayClientMessage(Component.literal("Error opening color picker."), false);
            FilamentCosmetics.LOGGER.error("Failed to open ColorPickerComponent", e);
        }
    }

    private static class ColorPickerScreen extends SimpleGui {
        private final ServerPlayer player;
        private final ItemStack hatItemStack;
        private final Consumer<ItemStack> onColorSelectCallback;

        private final MutableFloat saturation = new MutableFloat(100F);
        private final MutableInt selectedBaseColorSlotIndex = new MutableInt(0);
        private final MutableBoolean usePaintBrushView = new MutableBoolean(true);
        private boolean initialGradientDrawn = false;


        public ColorPickerScreen(ServerPlayer player, ItemStack hatItemStack, Consumer<ItemStack> onColorSelectCallback) {
            super(MenuType.GENERIC_9x5, player, true);
            this.player = player;
            this.hatItemStack = hatItemStack.copy();
            this.onColorSelectCallback = onColorSelectCallback;

            this.setTitle(GuiTextures.COLOR_PICKER_MENU.apply(CosmeticsGUIConfig.getColorPickerGUIName()));
            populateGui();
        }

        private void populateGui() {
            this.setSlot(CosmeticsGUIConfig.colorInputSlot, GuiElementBuilder.from(hatItemStack));
            drawBaseColorSlots();
            if (!initialGradientDrawn && CosmeticsGUIConfig.colorSlots.length > 0) {
                selectedBaseColorSlotIndex.setValue(CosmeticsGUIConfig.colorSlots[0]);
                drawGradientSlots();
                initialGradientDrawn = true;
            }
            setupBrightnessButtons();
            setupViewToggleButtons();
            setupColorInputButton();
        }

        public void drawBaseColorSlots() {
            ItemStack templateStack;
            if (usePaintBrushView.getValue() && CosmeticsGUIConfig.paintItemPolymerModelData != null) {
                templateStack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
                templateStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(CosmeticsGUIConfig.paintItemPolymerModelData.value()));
            } else {
                templateStack = hatItemStack.copy();
                templateStack.remove(DataComponents.DYED_COLOR);
            }

            int[] baseColorDisplaySlots = CosmeticsGUIConfig.colorSlots;
            String[] colorHexValues = CosmeticsGUIConfig.colorHexValues;

            for (int i = 0; i < baseColorDisplaySlots.length && i < colorHexValues.length; i++) {
                ItemStack displayColorStack = templateStack.copy();
                try {
                    int decimalColor = Integer.parseInt(colorHexValues[i], 16);
                    displayColorStack.set(DataComponents.DYED_COLOR, new DyedItemColor(decimalColor, true));

                    CompoundTag nbt = new CompoundTag();
                    nbt.putInt("baseColorHexIndex", i);
                    displayColorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

                    final int finalI = i;
                    this.setSlot(baseColorDisplaySlots[i], GuiElementBuilder.from(displayColorStack)
                            .setCallback((clickIndex, clickType, actionType) -> {
                                selectedBaseColorSlotIndex.setValue(finalI);
                                drawGradientSlots();
                            })
                    );
                } catch (NumberFormatException e) {
                    FilamentCosmetics.LOGGER.warn("Invalid hex color in config: {}", colorHexValues[i]);
                }
            }
            if (!initialGradientDrawn && baseColorDisplaySlots.length > 0) {
                selectedBaseColorSlotIndex.setValue(0);
                drawGradientSlots();
                initialGradientDrawn = true;
            }
        }

        private void drawGradientSlots() {
            if (selectedBaseColorSlotIndex.getValue() < 0 || selectedBaseColorSlotIndex.getValue() >= CosmeticsGUIConfig.colorHexValues.length) {
                return;
            }
            String baseHex = CosmeticsGUIConfig.colorHexValues[selectedBaseColorSlotIndex.getValue()];
            Color baseColor;
            try {
                baseColor = new Color(Integer.parseInt(baseHex, 16));
            } catch (NumberFormatException e) {
                FilamentCosmetics.LOGGER.warn("Invalid base hex for gradient: {}", baseHex);
                return;
            }

            ItemStack gradientItem;
            if(usePaintBrushView.getValue()){
                gradientItem = new ItemStack(Items.LEATHER_HORSE_ARMOR);
                gradientItem.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(CosmeticsGUIConfig.paintItemPolymerModelData.value()));
            } else {
                gradientItem = hatItemStack.copy();
            }

            float[] hsv = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
            int[] gradientDisplaySlots = CosmeticsGUIConfig.colorGradientSlots;

            for (int j = 0; j < gradientDisplaySlots.length; j++) {
                float brightnessFactor = (1.0f / (gradientDisplaySlots.length + 1)) * (j + 1.0f);
                brightnessFactor = Math.min(Math.max(brightnessFactor, 0.1f), 1.0f);

                Color gradientStepColor = (baseColor.getRed() == baseColor.getGreen() && baseColor.getRed() == baseColor.getBlue())
                        ? new Color(Color.HSBtoRGB(hsv[0], 0, brightnessFactor))
                        : new Color(Color.HSBtoRGB(hsv[0], saturation.getValue() / 100F, brightnessFactor));

                int stepColorRgb = gradientStepColor.getRGB();
                gradientItem.set(DataComponents.DYED_COLOR, new DyedItemColor(stepColorRgb, true));

                this.setSlot(gradientDisplaySlots[j], GuiElementBuilder.from(gradientItem)
                        .setCallback(() -> {
                            ItemStack finalColoredHat = hatItemStack.copy();
                            finalColoredHat.set(DataComponents.DYED_COLOR, new DyedItemColor(stepColorRgb, true));

                            this.setSlot(CosmeticsGUIConfig.colorOutputSlot, GuiElementBuilder.from(finalColoredHat.copy())
                                    .setName(Component.literal("Click to Confirm"))
                                    .setCallback(() -> {
                                        this.close();
                                        onColorSelectCallback.accept(finalColoredHat);
                                    })
                            );
                        })
                );
            }
        }

        public void setupBrightnessButtons() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("decreaseBrightness"), () -> {
                saturation.subtract(CosmeticsGUIConfig.saturationAdjustmentValue);
                if (saturation.getValue() < 15F) saturation.setValue(15F);
                drawGradientSlots();
            });

            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("increaseBrightness"), () -> {
                saturation.add(CosmeticsGUIConfig.saturationAdjustmentValue);
                if (saturation.getValue() > 100F) saturation.setValue(100F);
                drawGradientSlots();
            });
        }

        public void setupViewToggleButtons() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("toggleColorView"), () -> {
                usePaintBrushView.setValue(!usePaintBrushView.getValue());
                drawBaseColorSlots();
            });
        }

        public void setupColorInputButton() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("enterColor"),
                    () -> new ColorInputSign(player, hatItemStack, onColorSelectCallback).open()
            );
        }
    }

    private static class ColorInputSign extends SignGui {
        private final ItemStack itemToColor;
        private final Consumer<ItemStack> onColorSelectCallback;

        public ColorInputSign(ServerPlayer player, ItemStack itemToColor, Consumer<ItemStack> onColorSelectCallback) {
            super(player);
            this.itemToColor = itemToColor;
            this.onColorSelectCallback = onColorSelectCallback;

            this.setSignType(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(CosmeticsGUIConfig.signType)));
            this.setColor(CosmeticsGUIConfig.signColor);
            List<String> lines = CosmeticsGUIConfig.getTextLines();
            for (int i = 0; i < lines.size() && i < 4; i++) {
                this.setLine(i, Component.literal(lines.get(i)));
            }
        }

        @Override
        public void onClose() {
            String colorString = this.getLine(0).getString().trim();

            if (colorString.isEmpty()) {
                return;
            }

            try {
                // Allow formats like "FFFFFF" and "#FFFFFF"
                if (colorString.length() == 6 && !colorString.startsWith("#")) {
                    colorString = "#" + colorString;
                }
                Color color = Color.decode(colorString);

                ItemStack coloredStack = itemToColor.copy();
                coloredStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getRGB(), true));

                this.player.displayClientMessage(CosmeticsGUIConfig.getSuccessColorChangeMessage(), false);

                onColorSelectCallback.accept(coloredStack);

            } catch (NumberFormatException e) {
                this.player.displayClientMessage(CosmeticsGUIConfig.getErrorColorChangeMessage(), false);
            }
        }
    }
}