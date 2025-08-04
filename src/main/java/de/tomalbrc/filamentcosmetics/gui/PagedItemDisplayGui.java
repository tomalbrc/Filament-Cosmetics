package de.tomalbrc.filamentcosmetics.gui;

import de.tomalbrc.filamentcosmetics.config.AbstractGuiConfig;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.gui.actions.OpenColorPickerAction;
import de.tomalbrc.filamentcosmetics.gui.core.ICosmeticProvider;
import de.tomalbrc.filamentcosmetics.gui.core.IItemAction;
import de.tomalbrc.filamentcosmetics.gui.filters.FilterManager;
import de.tomalbrc.filamentcosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PagedItemDisplayGui extends SimpleGui {

    private ICosmeticProvider provider;
    private IItemAction defaultClickAction;
    private final AbstractGuiConfig guiConfig;
    private int currentPage = 0;

    public final FilterManager filterManager;

    public PagedItemDisplayGui(ServerPlayer player, AbstractGuiConfig config, ICosmeticProvider provider, IItemAction defaultClickAction) {
        super(config.screenHandlerType, player, config.replaceInventory);
        this.guiConfig = config;
        this.provider = provider;
        this.defaultClickAction = defaultClickAction;
        this.filterManager = new FilterManager(this);
        setTitle(config.getGuiName());
        populateGui();
    }

    public void reinitialize(ICosmeticProvider newProvider, IItemAction newAction) {
        this.provider = newProvider;
        this.defaultClickAction = newAction;
        this.currentPage = 0;
        this.populateGui();
    }

    public void populateGui() {
        List<CustomItemEntry> allItems = provider.getItems();

        Predicate<CustomItemEntry> combinedFilter = filterManager.getCombinedPredicate();
        List<CustomItemEntry> filteredItems = allItems.stream()
                .filter(combinedFilter)
                .sorted(Comparator.comparing(CustomItemEntry::id))
                .collect(Collectors.toList());

        drawItems(filteredItems);

        setupNavigation(filteredItems.size());
        filterManager.drawFilterButtons();
    }

    private void drawItems(List<CustomItemEntry> itemsToDisplay) {
        int[] displaySlots = guiConfig.displaySlots;
        int itemsPerPage = displaySlots.length;
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            int slot = displaySlots[i];

            if (itemIndex < itemsToDisplay.size()) {
                CustomItemEntry entry = itemsToDisplay.get(itemIndex);
                ItemStack displayStack = entry.itemStack().copy();
                GuiElementBuilder element = new GuiElementBuilder(displayStack);

                if (Permissions.check(player, entry.permission())) {
                    element.addLoreLine(guiConfig.getMessageUnlocked());
                    element.setCallback(() -> {
                        determineAction(entry).execute(player, entry, this);
                    });
                } else {
                    element.addLoreLine(guiConfig.getMessageLocked());
                }
                setSlot(slot, element);
            } else {
                clearSlot(slot);
            }
        }
    }

    private IItemAction determineAction(CustomItemEntry entry) {
        Item baseItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.baseItemForModel()));
        if (Items.LEATHER_HORSE_ARMOR.equals(baseItem)) {
            return new OpenColorPickerAction();
        }
        return defaultClickAction;
    }

    private void setupNavigation(int totalFilteredItems) {
        int itemsPerPage = guiConfig.displaySlots.length;

        // Next Button
        if ((currentPage + 1) * itemsPerPage < totalFilteredItems) {
            GUIUtils.setUpButton(this, guiConfig.getButtonConfig("next"), () -> {
                currentPage++;
                populateGui();
            });
        }

        // Previous Button
        if (currentPage > 0) {
            GUIUtils.setUpButton(this, guiConfig.getButtonConfig("previous"), () -> {
                currentPage--;
                populateGui();
            });
        }
    }

    public void onFilterStateChanged() {
        this.currentPage = 0;
        this.populateGui();
    }
}