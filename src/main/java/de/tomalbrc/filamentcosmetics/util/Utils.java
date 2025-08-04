package de.tomalbrc.filamentcosmetics.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.gui.ColorPickerComponent;
import de.tomalbrc.filamentcosmetics.gui.actions.EquipCosmeticAction;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Collections;

public class Utils {
    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    public static Component formatDisplayName(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\' && i < st.length() - 1) {
                char nextChar = st.charAt(i + 1);
                if (nextChar == 'u') {
                    String hex = st.substring(i + 2, i + 6);
                    ch = (char) Integer.parseInt(hex, 16);
                    i += 5;
                }
            }
            sb.append(ch);
        }
        String sf = sb.toString().replace("§", "&");
        String formatted = LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(SERIALIZER.serialize(MINI_MESSAGE.deserialize(sf))));

        return Component.nullToEmpty(formatted);
    }

    public static int wearCosmeticById(CommandContext<CommandSourceStack> context) {
        final ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Invalid player specified."));
            return 1;
        }

        String id = StringArgumentType.getString(context, "cosmeticId");
        if (id == null || id.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Invalid cosmetic ID."));
            return 1;
        }

        CustomItemEntry entry = CustomItemRegistry.getCosmetic(id);

        if (entry == null) {
            context.getSource().sendSuccess(() -> Component.literal("Cosmetic not found with ID: " + id), false);
            return 1;
        }

        if (!Permissions.check(player, entry.permission())) {
            context.getSource().sendSuccess(() -> Component.literal("Selected player does not have permission to use this cosmetic."), false);
            return 1;
        }

        ItemStack cosmeticItem = entry.itemStack();
        if (cosmeticItem == null || cosmeticItem.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Cosmetic item definition is empty or invalid."));
            return 1;
        }

        boolean isColorable = Items.LEATHER_HORSE_ARMOR.equals(
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.baseItemForModel()))
        );

        if (isColorable) {
            ItemStack itemForColorPicker = cosmeticItem.copy();
            itemForColorPicker.remove(DataComponents.DYED_COLOR);

            new ColorPickerComponent(player, itemForColorPicker, (coloredStack) -> {
                var finalEntry = new CustomItemEntry(
                        entry.id(),
                        entry.permission(),
                        entry.displayName(),
                        Collections.emptyList(),
                        coloredStack,
                        entry.type(),
                        entry.baseItemForModel()
                );
                new EquipCosmeticAction().execute(player, finalEntry, null);
                context.getSource().sendSuccess(() -> Component.literal("Equipped colored cosmetic: " + finalEntry.displayName().getString()), false);
            }).open();

        } else {
            new EquipCosmeticAction().execute(player, entry, null);
            context.getSource().sendSuccess(() -> Component.literal("Equipped cosmetic: " + entry.displayName().getString()), false);
        }
        return 0;
    }
}
