package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EnumeratedRecord;

public class UserAssociationRecord {
    int user_entityID;
    String tag;
    int entityID;
    double value;
    String info;

    UserAssociationRecord(int userID, String assocTag, int otherID, double assocVal, String assocInfo) {
        user_entityID = userID;
        tag = assocTag;
        entityID = otherID;
        value = assocVal;
        info = assocInfo;
    }

    UserAssociationRecord(ResultSet mappingResult) {
        try {
            user_entityID = mappingResult.getInt("User_EntityID");
            tag = mappingResult.getString("Tag");
            int tempID = mappingResult.getInt("EntityID");
            if (mappingResult.wasNull())
                tempID = EnumeratedRecord.noID;
            entityID = tempID;
            double tempVal = mappingResult.getDouble("Value");
            if (mappingResult.wasNull())
                tempVal = Double.NaN;
            value = tempVal;
            String tempInfo = mappingResult.getString("Info");
            if (mappingResult.wasNull())
                tempInfo = null;
            info = tempInfo;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from user association result set", e);
        }
    }

    PreparedStatement prepareInsertStatement(Connection dataConnection) throws SQLException {
        PreparedStatement insertStatement = dataConnection.prepareStatement(getInsertString());
        insertStatement.setString(1, tag);
        if (info != null)
            insertStatement.setString(2, info);
        return insertStatement;
    }

    String getInsertString() {
        return " INSERT INTO UserAssociation ( User_EntityID, Tag, EntityID, Value, Info )\n" +
                "    VALUES (" + user_entityID + ", ?,"
                + (entityID == EnumeratedRecord.noID? "null" : entityID) + ", "
                + (Double.isNaN(value)? "null" : value) + ", "
                + (info == null? "null" : "?") + "); ";
    }
}
