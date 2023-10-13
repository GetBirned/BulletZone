package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

public class ItemPropertyRepository {
    static private HashMap<Integer, ItemProperty> propertyMap = new HashMap<Integer, ItemProperty>();
    static private HashMap<String, ItemProperty> nameToPropertyMap = new HashMap<>();

    final public ItemProperty None, Size, Weight, Price;
    final public ItemProperty Capacity, Armor, Damage, InstanceLimit;
    final public ItemProperty ElectricPower, ElectricPowerUsed, DrivePower, DrivePowerUsed;
    final public ItemProperty MovementToWeightRatio, WeightModifier, ArmorModifier, TerrainObstacleSensitivity;

    public ItemPropertyRepository(Connection dataConnection) {
        readStaticInfo(dataConnection);

        None = nameToPropertyMap.get("None");
        Size = nameToPropertyMap.get("Size");
        Weight = nameToPropertyMap.get("Weight");
        Price = nameToPropertyMap.get("Price");
        Capacity = nameToPropertyMap.get("Capacity");
        Armor = nameToPropertyMap.get("Armor");
        Damage= nameToPropertyMap.get("Damage");
        InstanceLimit = nameToPropertyMap.get("Instance Limit");
        ElectricPower= nameToPropertyMap.get("Electric Power");
        ElectricPowerUsed = nameToPropertyMap.get("Electric Power Used");
        DrivePower = nameToPropertyMap.get("Drive Power");
        DrivePowerUsed = nameToPropertyMap.get("Drive Power Used");
        MovementToWeightRatio = nameToPropertyMap.get("Movement-to-weight ratio");
        WeightModifier = nameToPropertyMap.get("Weight modifier");
        ArmorModifier = nameToPropertyMap.get("Armor modifier");
        TerrainObstacleSensitivity = nameToPropertyMap.get("Terrain-obstacle sensitivity");
    }

    /**
     * @return A collection of all ItemProperties in the database
     */
    public Collection<ItemProperty> getAll() {
        return propertyMap.values();
    }

    /**
     * Gives the ItemProperty associated with the passed name
     *
     * @param propertyName Name of the desired type (case-sensitive)
     * @return ItemType object corresponding to the typeName
     */
    public ItemProperty get(String propertyName) {
        return nameToPropertyMap.get(propertyName);
//        if (property == null)
//            throw new NullPointerException("Unable to resolve " + propertyName + " to a valid ItemProperty.");
//        return property;
    }

    /**
     * Gives the ItemProperty associated with the passed property ID
     *
     * @param propertyID ID of the desired type (from the database)
     * @return ItemType object corresponding to the typeName
     */
    public ItemProperty get(int propertyID) {
        return propertyMap.get(propertyID);
//        if (property == null)
//            throw new NullPointerException("Unable to resolve " + propertyID + " to a valid ItemProperty.");
//        return property;
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Reads the database and fills the HashMaps as appropriate with information about
     * ItemCategories and ItemTypes. Intended to be called once at time of initialization.
     *
     * @param dataConnection connection on which to make SQL queries
     */
    private void readStaticInfo(Connection dataConnection) {
        try {
            Statement statement = dataConnection.createStatement();

            ResultSet itemPropertyResult = statement.executeQuery("SELECT * FROM ItemProperty");
            while (itemPropertyResult.next()) {
                int id = itemPropertyResult.getInt("ItemPropertyID");
                int type = itemPropertyResult.getInt("ItemPropertyTypeID");
                String name = itemPropertyResult.getString("Name");
                ItemProperty ip = new ItemProperty(id, name, ItemProperty.PropertyType.values()[type]);
                propertyMap.put(id, ip);
                nameToPropertyMap.put(name, ip);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static Property info!", e);
        }
    }
}
