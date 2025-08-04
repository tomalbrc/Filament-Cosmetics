package de.tomalbrc.filamentcosmetics.util;

import de.tomalbrc.filamentcosmetics.config.ConfigManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GUIUtils {
    public static void setUpButton(SimpleGui gui, ConfigManager.NavigationButton buttonConfig, Runnable callback) {
        if (buttonConfig != null) {
            ItemStack itemStack;
            if(buttonConfig.model() != null) {
                itemStack = buttonConfig.baseItem().getDefaultInstance();
                itemStack.set(DataComponents.ITEM_MODEL, buttonConfig.model());
            } else {
                itemStack = new ItemStack(buttonConfig.baseItem());
            }

            GuiElementBuilder builder = new GuiElementBuilder(itemStack)
                    .setName(buttonConfig.name())
                    .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
                    .setCallback((index, clickType, actionType) -> callback.run());

            if (buttonConfig.model() != null) {
                builder.setComponent(DataComponents.ITEM_MODEL, buttonConfig.model());
            }

            gui.setSlot(buttonConfig.slotIndex(), builder);
        }
    }
}
