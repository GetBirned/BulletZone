package edu.unh.cs.cs619.bulletzone.datalayer.terrain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

public class TerrainTypeRepository {
    static private HashMap<Integer, TerrainType> terrainMap = new HashMap<>();
    static private HashMap<String, TerrainType> nameToTerrainMap = new HashMap<>();

    final public TerrainType Meadow, Rocky, Forest, Debris, Coast, OpenWater;
    final public TerrainType Earth, Bedrock;
    final public TerrainType Road, Building;
    final public TerrainType IndestructibleWall, Fortification, Wall;

    public TerrainTypeRepository(Connection dataConnection) {
        readStaticInfo(dataConnection);

        Meadow = nameToTerrainMap.get("Meadow");
        Rocky = nameToTerrainMap.get("Rocky");
        Forest = nameToTerrainMap.get("Forest");
        Debris = nameToTerrainMap.get("Debris");
        Coast = nameToTerrainMap.get("Coast");
        OpenWater = nameToTerrainMap.get("Open Water");
        Earth = nameToTerrainMap.get("Earth");
        Bedrock = nameToTerrainMap.get("Bedrock");
        Road = nameToTerrainMap.get("Road");
        Building = nameToTerrainMap.get("Building");
        IndestructibleWall = nameToTerrainMap.get("Indestructible Wall");
        Fortification = nameToTerrainMap.get("Fortification");
        Wall = nameToTerrainMap.get("Wall");
    }

    /**
     * @return A collection of all TerrainTypes in the database
     */
    public Collection<TerrainType> getAll() {
        return terrainMap.values();
    }

    /**
     * Gives the TerrainType associated with the passed name
     *
     * @param terrainTypeName Name of the desired type (case-sensitive)
     * @return ItemType object corresponding to the typeName
     */
    public TerrainType get(String terrainTypeName) {
        return nameToTerrainMap.get(terrainTypeName);
        //if (terrain == null)
        //    throw new NullPointerException("Unable to resolve " + terrainTypeName + " to a valid TerrainType.");
        //return terrain;
    }

    /**
     * Gives the TerrainType associated with the passed property ID
     *
     * @param terrainTypeID ID of the desired type (from the database)
     * @return ItemType object corresponding to the typeName
     */
    public TerrainType get(int terrainTypeID) {
        return terrainMap.get(terrainTypeID);
        //if (terrain == null)
        //    throw new NullPointerException("Unable to resolve " + terrainTypeID + " to a valid TerrainType.");
        //return terrain;
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Reads the database and fills the HashMaps as appropriate with information about
     * TerrainTypes. Intended to be called once at time of initialization.
     *
     * @param dataConnection connection on which to make SQL queries
     */
    private void readStaticInfo(Connection dataConnection) {
        try {
            Statement statement = dataConnection.createStatement();

            ResultSet terrainTypeResult = statement.executeQuery("SELECT * FROM TerrainType");
            while (terrainTypeResult.next()) {
                TerrainTypeRecord rec = new TerrainTypeRecord(terrainTypeResult);
                TerrainType tt = new TerrainType(rec);
                terrainMap.put(rec.terrainTypeID, tt);
                nameToTerrainMap.put(rec.name, tt);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static terrain type info!", e);
        }
    }
}
