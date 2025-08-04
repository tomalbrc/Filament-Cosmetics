package de.tomalbrc.filamentcosmetics.config.entries;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;


public record CustomItemEntry(String id, String permission, Component displayName, List<Component> lore, ItemStack itemStack,
                              ItemType type, String baseItemForModel) {
}
