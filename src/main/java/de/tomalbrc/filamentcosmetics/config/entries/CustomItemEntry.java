package de.tomalbrc.filamentcosmetics.config.entries;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;


public record CustomItemEntry(String id, String permission, Text displayName, List<Text> lore, ItemStack itemStack,
                              ItemType type, String baseItemForModel) {
}
