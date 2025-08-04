package de.tomalbrc.filamentcosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.config.ItemSkinsGUIConfig;
import de.tomalbrc.filamentcosmetics.gui.actions.ApplySkinAction;
import de.tomalbrc.filamentcosmetics.gui.filters.PermissionFilter;
import de.tomalbrc.filamentcosmetics.gui.providers.ItemSkinProvider;
import de.tomalbrc.filamentcosmetics.util.GUIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import static de.tomalbrc.filamentcosmetics.config.ConfigManager.ITEM_SKINS_GUI_CONFIG;

public class ItemSkinsGUI {
    public static int openItemSkinsGui(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            ItemStack handStack = player.getMainHandItem();
            var config = ITEM_SKINS_GUI_CONFIG;
            var provider = new ItemSkinProvider(handStack.getItem());
            var action = new ApplySkinAction(handStack, ItemSkinsGUIConfig.itemSlot);
            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action) {
                @Override
                public boolean onAnyClick(int idx, ClickType ct, net.minecraft.world.inventory.ClickType sa) {
                    if (idx >= this.getVirtualSize()) {
                        ItemStack newClicked = this.player.containerMenu.getSlot(idx).getItem();
                        if (!newClicked.isEmpty()) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                            var newProvider = new ItemSkinProvider(newClicked.getItem());
                            var newAction = new ApplySkinAction(newClicked, ItemSkinsGUIConfig.itemSlot);
                            this.reinitialize(newProvider, newAction);

                            setupDynamicSlots(this, newClicked);
                        }
                    }
                    return super.onAnyClick(idx, ct, sa);
                }
            };
            gui.filterManager.addFilter(
                    "permission",
                    new PermissionFilter(player),
                    config.getButtonConfig("filter.show-all-skins"),
                    config.getButtonConfig("filter.show-owned-skins"),
                    false
            );

            gui.setSlot(ItemSkinsGUIConfig.itemSlot, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Select an Item"))
                    .addLoreLine(Component.literal("Click an item in your inventory below.")));

            setupDynamicSlots(gui, handStack);
            gui.reinitialize(provider, action);

            gui.setLockPlayerInventory(true);
            gui.open();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred opening the Item Skins GUI. See console for details."));
            FilamentCosmetics.LOGGER.error("Failed to open item skins GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }


    private static void setupDynamicSlots(PagedItemDisplayGui gui, ItemStack targetStack) {
        var config = ITEM_SKINS_GUI_CONFIG;

        gui.setSlot(ItemSkinsGUIConfig.itemSlot, targetStack.copy());

        GUIUtils.setUpButton(gui, config.getButtonConfig("removeSkin"), () -> {
                targetStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, comp -> comp.update(nbt -> nbt.remove("cosmeticItemId")));
                targetStack.remove(DataComponents.CUSTOM_MODEL_DATA);

                gui.setSlot(ItemSkinsGUIConfig.itemSlot, targetStack.copy());
        });
    }
}