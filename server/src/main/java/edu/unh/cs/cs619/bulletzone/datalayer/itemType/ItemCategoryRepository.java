package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

public class ItemCategoryRepository {
    HashMap<Integer, ItemCategory> categoryMap = new HashMap<Integer, ItemCategory>();
    HashMap<Integer, ItemCategory> baseCategoryMap = new HashMap<Integer, ItemCategory>();
    HashMap<String, ItemCategory> nameToCategoryMap = new HashMap<String, ItemCategory>();

    final public ItemCategory Artifact, Frame, Weapon, Generator, Engine, Drive;
    final public ItemCategory AntiGravity, WeaponBoostingGenerator, ShieldRepair;

    public ItemCategoryRepository(Connection dataConnection, ItemPropertyRepository properties) {
        readStaticInfo(dataConnection, properties);

        Artifact = nameToCategoryMap.get("Artifact");
        Frame = nameToCategoryMap.get("Frame");
        Weapon = nameToCategoryMap.get("Weapon");
        Generator = nameToCategoryMap.get("Generator");
        Engine = nameToCategoryMap.get("Engine");
        Drive = nameToCategoryMap.get("Drive");
        AntiGravity = nameToCategoryMap.get("Anti-gravity");
        WeaponBoostingGenerator = nameToCategoryMap.get("Weapon-boosting generator");
        ShieldRepair = nameToCategoryMap.get("Shield/Repair");
    }

    /**
     * @param categoryID    Category ID for the requested category (used in ItemTypes)
     * @return  ItemCategory corresponding to the passed categoryID
     */
    public ItemCategory get(int categoryID) { return categoryMap.get(categoryID); }

    /**
     * @return A collection of all ItemCategories in the database
     */
    public Collection<ItemCategory> getAll() { return baseCategoryMap.values(); }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Reads the database and fills the HashMaps as appropriate with information about
     * ItemCategories and ItemTypes. Intended to be called once at time of initialization.
     *
     * @param dataConnection connection on which to make SQL queries
     */
    private void readStaticInfo(Connection dataConnection, ItemPropertyRepository properties) {
        try {
            Statement statement = dataConnection.createStatement();

            ResultSet itemCategoryResult = statement.executeQuery("SELECT * FROM ItemCategory");
            while (itemCategoryResult.next()) {
                int id = itemCategoryResult.getInt("ItemCategoryID");
                ItemProperty categoryProperties[] = new ItemProperty[3];
                for (int i = 0; i < 3; i++)
                    categoryProperties[i] = properties.get(itemCategoryResult.getInt("ItemPropertyID" + (i + 1)));
                String name = itemCategoryResult.getString("Name");
                ItemCategory ic = new ItemCategory(id, name, properties, categoryProperties);
                categoryMap.put(id, ic);
                nameToCategoryMap.put(name, ic);
                if (id > 0 && id < 10)
                    baseCategoryMap.put(id, ic);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static category info!", e);
        }
    }
}
