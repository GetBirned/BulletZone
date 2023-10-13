package edu.unh.cs.cs619.bulletzone.datalayer.terrain;

public class TerrainType {
    protected final TerrainTypeRecord info;

    TerrainType(TerrainTypeRecord rec) {
        info = rec;
    }

    @Override
    public String toString() { return info.name; }

    public int getID() {
        return info.terrainTypeID;
    }

    public String getName() {
        return info.name;
    }

    public boolean isSolid() { return info.solid; }

    public boolean isLiquid() { return info.liquid; }

    public double getMaxSize() { return info.maxSize; }

    public double getDifficulty() {
        return info.difficulty;
    }

    public double getStrength() {
        return info.strength;
    }

    public double getHardness() { return info.hardness; }

    public double getDamage() { return info.damage; }

}
