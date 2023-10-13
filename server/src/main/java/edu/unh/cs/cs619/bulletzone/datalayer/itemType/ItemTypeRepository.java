/**
 * Internal package class for interpreting (and creating?) database data corresponding to ItemTypes
 */
package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

public class ItemTypeRepository {
    HashMap<Integer, ItemType> typeMap = new HashMap<Integer, ItemType>();
    HashMap<Integer, ItemType> frameMap = new HashMap<Integer, ItemType>();
    HashMap<Integer, ItemType> weaponMap = new HashMap<Integer, ItemType>();
    HashMap<Integer, ItemType> generatorMap = new HashMap<Integer, ItemType>();
    HashMap<Integer, ItemType> engineMap = new HashMap<Integer, ItemType>();
    HashMap<Integer, ItemType> driveMap = new HashMap<Integer, ItemType>();
    HashMap<String, ItemType> nameToTypeMap = new HashMap<>();
    HashMap<String, HashMap<Integer, ItemType> >  categoryItems = new HashMap<String, HashMap<Integer, ItemType> >();

    final public ItemType GarageBay, StorageContainer, Trifle, Trinket, Thingamajig;
    final public ItemType TransportFrame, TankFrame, TruckFrame, BattleSuit, ShipFrame;
    final public ItemType VehicleExpansionFrame, BattleSuitExpansionFrame;
    final public ItemType TankCannon, PlasmaCannon, ParticleBeamGun, LargePlasmaCannon;
    final public ItemType TankGenerator, TruckGenerator, PortableGenerator, ShipGenerator;
    final public ItemType TankEngine, TruckEngine, BattleSuitPowerConverter, ShipEngine;
    final public ItemType TankDriveTracks, TruckDriveTracks, BattleSuitLegAssists, ShipDriveImpellers;
    final public ItemType GravAssist, FusionGenerator, DeflectorShield, AutomatedRepairKit;

    public ItemTypeRepository(Connection dataConnection, ItemCategoryRepository categories) {
        readStaticInfo(dataConnection, categories);

        GarageBay = nameToTypeMap.get("Garage bay");
        StorageContainer = nameToTypeMap.get("Storage container");
        Trifle = nameToTypeMap.get("Trifle");
        Trinket = nameToTypeMap.get("Trinket");
        Thingamajig = nameToTypeMap.get("Thingamajig");
        TransportFrame = nameToTypeMap.get("Transport frame");
        TankFrame = nameToTypeMap.get("Standard tank frame");
        TruckFrame = nameToTypeMap.get("Standard truck frame");
        BattleSuit = nameToTypeMap.get("Standard battle suit");
        ShipFrame = nameToTypeMap.get("Standard ship frame");
        VehicleExpansionFrame = nameToTypeMap.get("Vehicle expansion frame");
        BattleSuitExpansionFrame = nameToTypeMap.get("Battle suit expansion frame");
        TankCannon = nameToTypeMap.get("Standard tank cannon");
        PlasmaCannon = nameToTypeMap.get("Plasma cannon");
        ParticleBeamGun = nameToTypeMap.get("Particle beam gun");
        LargePlasmaCannon = nameToTypeMap.get("Large Plasma cannon");
        TankGenerator = nameToTypeMap.get("Standard tank generator");
        TruckGenerator = nameToTypeMap.get("Standard truck generator");
        PortableGenerator = nameToTypeMap.get("Portable generator");
        ShipGenerator = nameToTypeMap.get("Standard ship generator");
        TankEngine = nameToTypeMap.get("Standard tank engine");
        TruckEngine = nameToTypeMap.get("Standard truck engine");
        BattleSuitPowerConverter = nameToTypeMap.get("Battle-suit power converter");
        ShipEngine = nameToTypeMap.get("Standard ship engine");
        TankDriveTracks = nameToTypeMap.get("Standard tank drive tracks");
        TruckDriveTracks = nameToTypeMap.get("Standard truck drive wheels");
        BattleSuitLegAssists = nameToTypeMap.get("Battle-suit leg assists");
        ShipDriveImpellers = nameToTypeMap.get("Standard ship drive impellers");
        GravAssist = nameToTypeMap.get("Grav-assist");
        FusionGenerator = nameToTypeMap.get("Fusion generator");
        DeflectorShield = nameToTypeMap.get("Deflector shield");
        AutomatedRepairKit = nameToTypeMap.get("Automated repair kit");

        categoryItems.put("Frame", frameMap);
        categoryItems.put("Weapon", weaponMap);
        categoryItems.put("Drive", driveMap);
        categoryItems.put("Generator", generatorMap);
        categoryItems.put("Engine", engineMap);
    }

    /**
     * Gives the ItemType associated with the passed name
     * @param typeName  Name of the desired type (case-sensitive)
     * @return  ItemType object corresponding to the typeName
     */
    public ItemType get(String typeName) {
        return nameToTypeMap.get(typeName);
//        if (type == null)
//            throw new NullPointerException("Unable to resolve " + typeName + " to a valid type of game item.");
//        return type;
    }

    /**
     * @return A collection of all ItemTypes in the database
     */
    public Collection<ItemType> getAll() {
        return typeMap.values();
    }

    /**
     * Returns a collection of all ItemTypes in the passed category
     * @param category  Name of the desired category
     * @return  Collection of ItemTypes in the category
     */
    public Collection<ItemType> getInCategory(String category) { return categoryItems.get(category).values(); }

    /**
     * @param typeID    Type ID for the requested type (used in other objects)
     * @return  ItemType corresponding to the passed typeID
     */
    public ItemType get(int typeID) { return typeMap.get(typeID); }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Reads the database and fills the HashMaps as appropriate with information about
     * ItemCategories and ItemTypes. Intended to be called once at time of initialization.
     *
     * @param dataConnection connection on which to make SQL queries
     */
    private void readStaticInfo(Connection dataConnection, ItemCategoryRepository categories) {
        try {
            Statement statement = dataConnection.createStatement();

            ResultSet itemTypeResult = statement.executeQuery("SELECT * FROM ItemType");
            while (itemTypeResult.next()) {
                ItemTypeRecord rec = new ItemTypeRecord(itemTypeResult, categories);
                recordItemType(rec);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static type info!", e);
        }
    }

    /**
     * Internal factory method for generating an ItemType based on information in the passed
     * ItemTypeRecord and inserting it into the appropriate HashMap.
     * @param rec   reference to an ItemTypeRecord that's the result of an SQL query
     */
    private void recordItemType(ItemTypeRecord rec) {
        ItemType itemType;
        int id = rec.itemTypeID;
        boolean idFrame = (rec.category.getName() == "Frame");
        itemType = new ItemType(rec);
        typeMap.put(id, itemType);
        nameToTypeMap.put(rec.name, itemType);
        switch (rec.category.getName())
        {
            case "Frame":
                frameMap.put(id, itemType);
                break;
            case "Weapon":
                weaponMap.put(id, itemType);
                break;
            case "Generator":
                generatorMap.put(id, itemType);
                break;
            case "Engine":
                engineMap.put(id, itemType);
                break;
            case "Drive":
                driveMap.put(id, itemType);
                break;
            //Note: Artifacts do not show up as a category
        }
    }
}
