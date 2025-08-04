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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static de.tomalbrc.filamentcosmetics.config.ConfigManager.ITEM_SKINS_GUI_CONFIG;

public class ItemSkinsGUI {
    public static int openItemSkinsGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            ItemStack handStack = player.getMainHandStack();
            var config = ITEM_SKINS_GUI_CONFIG;
            var provider = new ItemSkinProvider(handStack.getItem());
            var action = new ApplySkinAction(handStack, ItemSkinsGUIConfig.getItemSlot());
            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action) {
                @Override
                public boolean onAnyClick(int idx, ClickType ct, SlotActionType sa) {
                    if (idx >= this.getVirtualSize()) {
                        ItemStack newClicked = this.player.currentScreenHandler.getSlot(idx).getStack();
                        if (!newClicked.isEmpty()) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                            var newProvider = new ItemSkinProvider(newClicked.getItem());
                            var newAction = new ApplySkinAction(newClicked, ItemSkinsGUIConfig.getItemSlot());
                            this.reinitialize(newProvider, newAction);

                            setupDynamicSlots(this, newClicked);
                        }
                    }
                    return super.onAnyClick(idx, ct, sa);
                }
            };
            gui.getFilterManager().addFilter(
                    "permission",
                    new PermissionFilter(player),
                    config.getButtonConfig("filter.show-all-skins"),
                    config.getButtonConfig("filter.show-owned-skins"),
                    false
            );

            gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Select an Item"))
                    .addLoreLine(Text.literal("Click an item in your inventory below.")));

            setupDynamicSlots(gui, handStack);
            gui.reinitialize(provider, action);

            gui.setLockPlayerInventory(true);
            gui.open();
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Item Skins GUI. See console for details."));
            FilamentCosmetics.LOGGER.error("Failed to open item skins GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }


    private static void setupDynamicSlots(PagedItemDisplayGui gui, ItemStack targetStack) {
        var config = ITEM_SKINS_GUI_CONFIG;

        gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), targetStack.copy());

        GUIUtils.setUpButton(gui, config.getButtonConfig("removeSkin"), () -> {
                targetStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> nbt.remove("cosmeticItemId")));
                targetStack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);

                gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), targetStack.copy());
        });
    }
}