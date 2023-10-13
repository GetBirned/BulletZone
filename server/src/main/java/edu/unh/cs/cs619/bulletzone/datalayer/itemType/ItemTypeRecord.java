/**
 * Internal package structure for representing data coming out of a SQL query on the ItemType table
 */
package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

import java.sql.ResultSet;
import java.sql.SQLException;

class ItemTypeRecord {
    int itemTypeID;
    public String name;
    ItemCategory category;
    double val[];

    ItemTypeRecord(ResultSet itemTypeResult, ItemCategoryRepository categories) {
        try {
            itemTypeID = itemTypeResult.getInt("ItemTypeID");
            name = itemTypeResult.getString("Name");
            category = categories.get(itemTypeResult.getInt("ItemCategoryID"));
            val = new double[ItemProperty.ID.values().length];
            for (ItemProperty.ID id : ItemProperty.ID.values())
                val[id.ordinal()] = itemTypeResult.getDouble(id.name());
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from item type result set", e);
        }
    }
}
