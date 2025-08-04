package de.tomalbrc.filamentcosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import de.tomalbrc.filamentcosmetics.FilamentCosmetics;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import de.tomalbrc.filamentcosmetics.config.entries.CustomItemRegistry;
import de.tomalbrc.filamentcosmetics.config.entries.ItemType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
    private static final Dao<CosmeticTable, Integer> cosmeticDao;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL);
            TableUtils.createTableIfNotExists(connectionSource, CosmeticTable.class);
            cosmeticDao = DaoManager.createDao(connectionSource, CosmeticTable.class);
        } catch (Exception e) {
            FilamentCosmetics.LOGGER.error("Failed to initialize database", e);
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public static void init() {}

    public static void setCosmetic(ServerPlayer player, ItemType type, ItemStack itemStack) {
        try {
            CosmeticTable existingEntry = findEntry(player.getStringUUID(), type);

            if (itemStack == null || itemStack.isEmpty()) {
                if (existingEntry != null) {
                    cosmeticDao.delete(existingEntry);
                }
                return;
            }

            String cosmeticId = getCosmeticIdFromStack(itemStack);
            if (cosmeticId == null) {
                FilamentCosmetics.LOGGER.warn("Attempted to set a cosmetic with an ItemStack lacking a 'cosmeticItemId' for player {}", player.getStringUUID());
                if (existingEntry != null) {
                    cosmeticDao.delete(existingEntry);
                }
                return;
            }

            Integer dyedColor = getDyedColorFromStack(itemStack);

            if (existingEntry != null) {
                existingEntry.cosmeticId = cosmeticId;
                existingEntry.dyedColor = dyedColor;
                cosmeticDao.update(existingEntry);
            } else {
                CosmeticTable newEntry = new CosmeticTable();
                newEntry.uuid = (player.getStringUUID());
                newEntry.cosmeticType = (type.toString());
                newEntry.cosmeticId = (cosmeticId);
                newEntry.dyedColor = (dyedColor);
                cosmeticDao.create(newEntry);
            }
        } catch (SQLException e) {
            FilamentCosmetics.LOGGER.error("Error saving cosmetic data for player {} and type {}", player.getStringUUID(), type, e);
            throw new RuntimeException("Error saving cosmetic data", e);
        }
    }

    public static ItemStack getCosmetic(ServerPlayer player, ItemType type) {
        try {
            CosmeticTable cosmeticData = findEntry(player.getStringUUID(), type);

            if (cosmeticData == null || cosmeticData.cosmeticId == null) {
                return ItemStack.EMPTY;
            }

            CustomItemEntry cosmeticDefinition = CustomItemRegistry.getCosmetic(cosmeticData.cosmeticId);
            if (cosmeticDefinition == null) {
                FilamentCosmetics.LOGGER.warn("Player {} has cosmetic '{}' equipped, but it's no longer registered.", player.getName().getString(), cosmeticData.cosmeticId);
                return ItemStack.EMPTY;
            }

            if (!Permissions.check(player, cosmeticDefinition.permission())) {
                return ItemStack.EMPTY;
            }

            ItemStack cosmeticStack = cosmeticDefinition.itemStack().copy();
            if (cosmeticData.dyedColor != null) {
                cosmeticStack.set(DataComponents.DYED_COLOR, new DyedItemColor(cosmeticData.dyedColor, true));
            }
            return cosmeticStack;
        } catch (SQLException e) {
            FilamentCosmetics.LOGGER.error("Error loading cosmetic data for player {} and type {}", player.getStringUUID(), type, e);
            throw new RuntimeException("Error loading cosmetic data", e);
        }
    }

    private static CosmeticTable findEntry(String playerUUID, ItemType type) throws SQLException {
        Map<String, Object> queryFields = new HashMap<>();
        queryFields.put("uuid", playerUUID);
        queryFields.put("cosmetic_type", type.toString());
        return cosmeticDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
    }

    private static String getCosmeticIdFromStack(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains("cosmeticItemId", CompoundTag.TAG_STRING)) {
                return nbt.getString("cosmeticItemId");
            }
        }
        return null;
    }

    private static Integer getDyedColorFromStack(ItemStack stack) {
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
        return dyedColor != null ? dyedColor.rgb() : null;
    }
}