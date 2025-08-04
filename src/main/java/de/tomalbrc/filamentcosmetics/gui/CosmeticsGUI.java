package de.tomalbrc.filamentcosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import de.tomalbrc.filamentcosmetics.gui.actions.EquipCosmeticAction;
import de.tomalbrc.filamentcosmetics.gui.filters.ItemTypeFilter;
import de.tomalbrc.filamentcosmetics.gui.filters.PermissionFilter;
import de.tomalbrc.filamentcosmetics.gui.providers.StandaloneCosmeticProvider;
import de.tomalbrc.filamentcosmetics.util.GUIUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static de.tomalbrc.filamentcosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;


public class CosmeticsGUI {

    public static int openGui(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            var config = COSMETICS_GUI_CONFIG;
            var provider = new StandaloneCosmeticProvider();
            var action = new EquipCosmeticAction();

            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action);

            gui.filterManager.addFilter(
                    "permission",
                    new PermissionFilter(player),
                    config.getButtonConfig("filter.show-all-skins"),
                    config.getButtonConfig("filter.show-owned-skins"),
                    false
            );

            gui.filterManager.addFilter(
                    "hat",
                    new ItemTypeFilter(ItemType.HAT),
                    config.getButtonConfig("filter.hats-disabled"),
                    config.getButtonConfig("filter.hats-enabled"),
                    true
            );

            gui.filterManager.addFilter(
                    "body-cosmetic",
                    new ItemTypeFilter(ItemType.BODY_COSMETIC),
                    config.getButtonConfig("filter.body-cosmetics-disabled"),
                    config.getButtonConfig("filter.body-cosmetics-enabled"),
                    false
            );

            GUIUtils.setUpButton(gui, config.getButtonConfig("removeSkin"), () -> {
                gui.close();
                action.execute(player, ItemStack.EMPTY, gui.filterManager.targetType);
            });

            gui.reinitialize(provider, action);
            gui.open();

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred opening the Cosmetics GUI. See console for details."));
            FilamentCosmetics.LOGGER.error("Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }
}