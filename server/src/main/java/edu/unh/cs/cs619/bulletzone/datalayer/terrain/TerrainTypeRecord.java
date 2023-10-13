package edu.unh.cs.cs619.bulletzone.datalayer.terrain;

import java.sql.ResultSet;
import java.sql.SQLException;

class TerrainTypeRecord {
    int terrainTypeID;
    int resourceTypeID;
    String name;
    boolean solid, liquid;
    double difficulty;
    double maxSize;
    double strength;
    double hardness;
    double damage;

    TerrainTypeRecord(ResultSet terrainTypeResult) {
        try {
            terrainTypeID = terrainTypeResult.getInt("TerrainTypeID");
            //resourceTypeID = terrainTypeResult.getInt("ResourceTypeID");
            name = terrainTypeResult.getString("Name");
            solid = terrainTypeResult.getBoolean("Solid");
            liquid = terrainTypeResult.getBoolean("Liquid");
            difficulty = terrainTypeResult.getFloat("Difficulty");
            maxSize = terrainTypeResult.getFloat("MaxSize");
            strength = terrainTypeResult.getFloat("Strength");
            hardness = terrainTypeResult.getFloat("Hardness");
            damage = terrainTypeResult.getFloat("Damage");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from terrain type result set", e);
        }
    }
}
