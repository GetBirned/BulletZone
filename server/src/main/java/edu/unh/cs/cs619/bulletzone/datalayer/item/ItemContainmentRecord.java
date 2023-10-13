package edu.unh.cs.cs619.bulletzone.datalayer.item;

import java.sql.ResultSet;
import java.sql.SQLException;

class ItemContainmentRecord {
    int container_entityID;
    int item_entityID;
    int startSlot;
    int endSlot;
    int modifier;

    ItemContainmentRecord(int icItemID, int icContainerID) {
        item_entityID = icItemID;
        container_entityID = icContainerID;
    }

    ItemContainmentRecord(ResultSet mappingResult) {
        try {
            container_entityID = mappingResult.getInt("Container_EntityID");
            item_entityID = mappingResult.getInt("Item_EntityID");
            startSlot = mappingResult.getInt("startSlot");
            endSlot = mappingResult.getInt("endSlot");
            modifier = mappingResult.getInt("modifier");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from item containment result set", e);
        }
    }

    String getInsertString() {
        return " INSERT INTO ItemContainer_Item ( Container_EntityID, Item_EntityID, StartSlot, EndSlot, Modifier )\n" +
                "    VALUES (" + container_entityID + ", "
                + item_entityID + ", "
                + startSlot + ", "
                + endSlot + ", "
                + modifier + "); ";
    }
}
