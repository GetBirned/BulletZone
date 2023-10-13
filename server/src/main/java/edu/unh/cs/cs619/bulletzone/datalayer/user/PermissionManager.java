package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.unh.cs.cs619.bulletzone.datalayer.BulletZoneData;
import edu.unh.cs.cs619.bulletzone.datalayer.core.Status;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.AggregateOwnableRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntity;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntityRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.Permission;

public class PermissionManager {
    BulletZoneData data;
    OwnableEntityRepository targetRepo;
    GameUserRepository userRepo;
    public class AccessibleTargets {
        final int maxPermissions = 4;
        public boolean hasPermission(int targetID, Permission p) {
            if (targetPermissions.containsKey(targetID))
                return targetPermissions.get(targetID).contains(p);
            else
                return false;
        }

        public Collection<OwnableEntity> getItems() {
            HashSet<OwnableEntity> items = new HashSet<>();
            for (int targetID: targetPermissions.keySet()) {
                OwnableEntity c = targetRepo.getTarget(targetID);
                if (c != null)
                    items.add(c);
            }
            return items;
        }

        public Collection<Permission> getPermissionsOnItem(int targetID) {
            if (targetPermissions.containsKey(targetID))
                return targetPermissions.get(targetID);
            else
                return new HashSet<Permission>();
        }

        //----Package-level methods---
        void addPermission(int targetID, Permission p) {
            if (!targetPermissions.containsKey(targetID))
                targetPermissions.put(targetID, new HashSet<>(maxPermissions));
            targetPermissions.get(targetID).add(p);
        }
        boolean removePermission(int targetID, Permission p) {
            if (targetPermissions.containsKey(targetID)) {
                HashSet<Permission> permSet = targetPermissions.get(targetID);
                permSet.remove(p);
                if (permSet.isEmpty())
                    targetPermissions.remove(targetID);
                return !targetPermissions.isEmpty(); //"true" indicates non-empty
            }
            return false; //"false" indicates it is empty
        }
        private final HashMap<Integer, HashSet<Permission>> targetPermissions = new HashMap<>();
    }
    HashMap<Integer, AccessibleTargets> permissions = new HashMap<>(); //&&&
    private final HashMap<Integer, Set<GameUser>> targetToPermissionHolders = new HashMap<>();

    public boolean setOwner(OwnableEntity target, GameUser user) {
        return setOwner(target.getId(), user.getId());
    }

    public boolean removeOwner(OwnableEntity target) {
        return removeOwner(target.getId(), target.getOwner().getId());
    }

    public boolean check(OwnableEntity target, GameUser user, Permission p) {
        return check(target.getId(), user.getId(), p);
    }

    public boolean grant(OwnableEntity target, GameUser user, Permission p) {
        return grant(target.getId(), user.getId(), p);
    }

    public boolean revoke(OwnableEntity target, GameUser user, Permission p) {
        return revoke(target.getId(), user.getId(), p);
    }

    public AccessibleTargets getUserPermissions(GameUser user) {
        return getUserPermissions(user.getId());
    }

    public AccessibleTargets getUserPermissions(int userID) {
        if (!permissions.containsKey(userID))
            permissions.put(userID, new AccessibleTargets());
        return permissions.get(userID);
    }

    public Collection<GameUser> getUsersWithPermissionsOn(OwnableEntity target) {
        return getUsersWithPermissionsOn(target.getId());
    }

    public Collection<GameUser> getUsersWithPermissionsOn(int itemID) {
        return targetToPermissionHolders.get(itemID);
    }
    /**
     * Deletes the all permission relationships between oldUser and item from the in-memory
     * representation and marks them as deleted in the database.
     * @param itemID    ID of the item to be marked as deleted
     * @param oldUserID ID of the user who no longer owns the item
     * @return  true if the operation was successful, and false otherwise.
     */
    public boolean removeOwner(int itemID, int oldUserID) {
        OwnableEntity item = targetRepo.getTarget(itemID);
        GameUser user = userRepo.getUser(oldUserID);
        if (item == null || user == null)
            return false;

        if (!deletePermission(itemID, oldUserID, Permission.Owner))
            return false;

        item.setOwner(null);
        user.removePermissionTarget(item);
        return true;
    }

    /**
     * Creates the owner-permission relationships between user and item from the in-memory
     * representation and in the database.
     * @param itemID    ID of the item to be marked as deleted
     * @param userID ID of the user who no longer owns the item
     * @return  true if the operation was successful, and false otherwise.
     */
    public boolean setOwner(int itemID, int userID) {
        OwnableEntity item = targetRepo.getTarget(itemID);
        GameUser user = userRepo.getUser(userID);
        if (item == null || user == null)
            return false;
        GameUser oldOwner = item.getOwner();
        if (oldOwner != null)
            removeOwner(itemID, oldOwner.getId());

        if (!insertPermission(itemID, userID, Permission.Owner))
            return false;

        item.setOwner(user);
        user.addPermissionTarget(item);
        return true;
    }

    /**
     * Determines whether or not the passed user has specified permission on the passed item.
     * @param itemID    ID of the item being checked
     * @param userID    ID of the user being checked
     * @param p         Permission being verified
     * @return  true if the user has the permission, false otherwise.
     */
    public boolean check(int itemID, int userID, Permission p){
        if (p == Permission.Owner){
            OwnableEntity item = targetRepo.getTarget(itemID);
            if (item == null)
                return false;
            return (item.getOwner().getId() == userID);
        }
        if (permissions.containsKey(userID)) {
            return permissions.get(userID).hasPermission(itemID, p);
        }
        return false;
    }

    /**
     * Grants the passed user the specified permission for the specified item
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is being given the permission
     * @param p         Permission being added
     * @return  true if the permission was granted, false otherwise (including if the
     *          permission was already active)
     */
    public boolean grant(int itemID, int userID, Permission p) {
        if (check(itemID, userID, p)) //if the permission is already active, do nothing
            return false;
        if (p == Permission.Owner){
            return setOwner(itemID, userID);
        }

        if (!insertPermission(itemID, userID, p))
            return false;

        return true;
    }

    /**
     * Revokes from the passed user the specified permission for the specified item
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is losing the permission
     * @param p         Permission being removed
     * @return  true if the permission was revoked, false otherwise (including if the
     *          permission was already not present)
     */
    public boolean revoke(int itemID, int userID, Permission p) {
        if (!check(itemID, userID, p)) //if the permission is already inactive, do nothing
            return false;
        if (p == Permission.Owner){
            return removeOwner(itemID, userID);
        }

        if (!deletePermission(itemID, userID, p))
            return false;

        return true;
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Update internal structure to account for a new permission
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is being given the permission
     * @param p         Permission being added
     */
    void addPermission(int itemID, int userID, Permission p) {
        if (!permissions.containsKey(userID))
            permissions.put(userID, new AccessibleTargets());
        permissions.get(userID).addPermission(itemID, p);
        if (!targetToPermissionHolders.containsKey(itemID))
            targetToPermissionHolders.put(itemID, new HashSet<GameUser>());
        targetToPermissionHolders.get(itemID).add(userRepo.getUser(userID));
    }

    /**
     * Update internal structure to account for a revoked permission
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is having the permission revoked
     * @param p         Permission being removed
     */
    void removePermission(int itemID, int userID, Permission p) {
        if (permissions.containsKey(userID)) {
            if (permissions.get(userID).removePermission(itemID, p) == false) //indicates empty
                targetToPermissionHolders.get(itemID).remove(userRepo.getUser(userID));
        }
    }

    /**
     * Insert a new permission record into the database
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is being given the permission
     * @param p         Permission being added
     */
    boolean insertPermission(int itemID, int userID, Permission p) {
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return false;

        OwnableEntityRecord rec = new OwnableEntityRecord(itemID, userID, p);

        try {
            PreparedStatement insertStatement = dataConnection.prepareStatement(rec.getInsertString());
            if (insertStatement.executeUpdate() == 0) {
                dataConnection.close();
                return false;
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Error while permission " + p.name() + ".", e);
        }
        addPermission(itemID, userID, p);
        return true;
    }

    /**
     * Mark a permission record in the database as deleted
     * @param itemID    ID of the item whose permissions are being changed
     * @param userID    ID of the user who is having the permission revoked
     * @param p         Permission being revoked
     */
    boolean deletePermission(int itemID, int userID, Permission p) {
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return false;
        Date date = new Date();

        try {
            PreparedStatement updateStatement = dataConnection.prepareStatement(
                    " UPDATE Entity_User_Permissions SET StatusID=" + Status.Deleted.ordinal() +
                            ", Deleted='" + new Timestamp(date.getTime()) +
                            "' WHERE EntityID=" + itemID + " AND User_EntityID=" + userID +
                            " AND PermissionID=" + p.ordinal() + "; ");
            if (updateStatement.executeUpdate() == 0) {
                dataConnection.close();
                return false; //nothing deleted
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Error while removing ownership.", e);
        }
        removePermission(itemID, userID, p);
        return true;
    }

    /**
     * Reads the database and fills the HashMaps as appropriate. Intended to be called once
     * at time of initialization.
     *
     * @param bzData       reference to BulletZoneData for making further SQL queries
     * @param repos     List of repositories of entities that can be owned
     * @param gameUserRepo repository of users that can own things
     */
    public void refresh(BulletZoneData bzData, List<? extends OwnableEntityRepository> repos, GameUserRepository gameUserRepo) {
        targetRepo = new AggregateOwnableRepository(repos);
        userRepo = gameUserRepo;
        data = bzData;
        targetToPermissionHolders.clear();
        permissions.clear();
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return;
        try {
            Statement statement = dataConnection.createStatement();

            // Read mapping of users to items that they own
            ResultSet mappingResult = statement.executeQuery(
                    "SELECT * FROM Entity_User_Permissions WHERE StatusID != "
                            + Status.Deleted.ordinal());
            Permission perms[] = Permission.values();
            while (mappingResult.next()) {
                int itemID = mappingResult.getInt("EntityID");
                int userID = mappingResult.getInt("User_EntityID");
                Permission permission = perms[mappingResult.getInt("PermissionID")];

                if (permission == Permission.Owner) {
                    // not worrying about StartSlot, EndSlot, or Modifier right now...
                    OwnableEntity entity = targetRepo.getTarget(itemID);
                    GameUser user = userRepo.getUser(userID);
                    if (user == null || entity == null) //could be null if user or entity were marked as deleted
                        continue; //just skip everything if something is null
                    user.addPermissionTarget(entity);
                    entity.setOwner(user);
                }
                addPermission(itemID, userID, permission);
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static info!", e);
        }
    }

}
