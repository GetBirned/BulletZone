/**
 * Internal package structure for representing data coming out of a SQL query on the Item table
 */
package edu.unh.cs.cs619.bulletzone.datalayer.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemType;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemTypeRepository;

class GameItemRecord extends EntityRecord {
    ItemType itemType;
    double usageMonitor;
    String originalName;
    double value;

    GameItemRecord(ItemType giItemType) {
        super(giItemType.isContainer() ? EntityType.ItemContainer : EntityType.Item);
        itemType = giItemType;
        usageMonitor = 0;
        value = Double.NaN;
    }

    GameItemRecord(ResultSet itemResult, ItemTypeRepository itemTypeRepo) {
        super(itemResult);
        try {
            itemType = itemTypeRepo.get(itemResult.getInt("ItemTypeID"));
            originalName = itemResult.getString("Name");
            if (itemResult.wasNull())
                originalName = null;
            usageMonitor = itemResult.getDouble("UsageMonitor");
            double tempVal = itemResult.getDouble("Value");
            if (itemResult.wasNull())
                tempVal = Double.NaN;
            value = tempVal;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from item result set", e);
        }
    }

    PreparedStatement prepareInsertStatement(Connection dataConnection) throws SQLException {
        PreparedStatement insertStatement = dataConnection.prepareStatement(getInsertString());
        if (originalName != null) {
            insertStatement.setString(1, originalName);
        }
        return insertStatement;
    }

    String getInsertString() {
        return " INSERT INTO Item ( EntityID, ItemTypeID, UsageMonitor, Name, Value )\n" +
                "    VALUES (" + getID() + ","
                + itemType.getID() + ", "
                + usageMonitor + ", "
                + (originalName == null? "null" : "?") + ", "
                + (Double.isNaN(value)? "null" : value) + "); ";
    }

    @Override
    public void insertInto(Connection dataConnection) throws SQLException {
        super.insertInto(dataConnection);
        PreparedStatement containerStatement = prepareInsertStatement(dataConnection);

        int affectedRows = containerStatement.executeUpdate();
        if (affectedRows == 0)
            throw new SQLException("Creating Item record for type " + itemType.getName() + " failed.");
    }

    void insertContainerInfoInto(String name, Connection dataConnection) throws SQLException {
        PreparedStatement containerStatement = dataConnection.prepareStatement(
                "INSERT INTO ItemContainer ( EntityID, Name ) VALUES ( " + getID() + ", ?);");
        containerStatement.setString(1, name);
        int affectedRows = containerStatement.executeUpdate();
        if (affectedRows == 0)
            throw new SQLException("Creating ItemContainer record for type " + itemType.getName() + " failed.");
    }
}