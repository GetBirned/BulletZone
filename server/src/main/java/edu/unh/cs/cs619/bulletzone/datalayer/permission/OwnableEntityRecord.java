package edu.unh.cs.cs619.bulletzone.datalayer.permission;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

public class OwnableEntityRecord extends EntityRecord {
    int userID;
    int permissionID;

    public OwnableEntityRecord(int pTargetID, int pUserID, Permission p) {
        super(EntityType.Invalid);
        setID(pTargetID);
        userID = pUserID;
        permissionID = p.ordinal();
    }

    public String getInsertString() {
        return " INSERT INTO Entity_User_Permissions ( EntityID, User_EntityID, PermissionID, StatusID, Created )\n" +
                "    VALUES (" + getID() + ", "
                + userID + ", "
                + permissionID + ", "
                + getStatusID() + ", '"
                + getCreated() + "'); ";
    }
}
