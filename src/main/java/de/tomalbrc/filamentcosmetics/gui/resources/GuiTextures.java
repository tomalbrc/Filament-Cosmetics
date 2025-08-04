package de.tomalbrc.filamentcosmetics.gui.resources;

import java.util.function.Function;
import net.minecraft.network.chat.Component;

import static de.tomalbrc.filamentcosmetics.gui.resources.UiResourceCreator.*;

public class GuiTextures {
    public static final Function<Component, Component> COLOR_PICKER_MENU = background("color_picker_menu");
    public static final Function<Component, Component> COSMETICS_MENU = background("cosmetics_menu");
    public static final Function<Component, Component> ITEM_SKINS_MENU = background("item_skins_menu");

    public static void register(){
    }
}
