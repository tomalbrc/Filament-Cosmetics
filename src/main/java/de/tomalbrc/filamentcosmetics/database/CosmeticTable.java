package de.tomalbrc.filamentcosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cosmetics")
public class CosmeticTable {
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(uniqueCombo = true, columnName = "uuid")
    public String uuid;

    @DatabaseField(uniqueCombo = true, columnName = "cosmetic_type")
    public String cosmeticType;

    @DatabaseField(columnName = "cosmetic_id")
    public String cosmeticId;

    @DatabaseField(columnName = "dyed_color")
    public Integer dyedColor;
}
