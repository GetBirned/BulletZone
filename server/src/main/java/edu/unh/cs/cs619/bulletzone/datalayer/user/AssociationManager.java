package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unh.cs.cs619.bulletzone.datalayer.BulletZoneData;
import edu.unh.cs.cs619.bulletzone.datalayer.core.AggregateRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.core.Entity;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EnumeratedRecord;

public class AssociationManager {
    BulletZoneData data;
    GameUserRepository userRepo;
    EntityRepository entityRepo;
    HashMap<String, HashMap<Integer, UserAssociation>> associations = new HashMap<>();

    /** Add a named association between a user and some other entity (item, user, account)
     * @param user              Key user with the association
     * @param associationType   Tag for the association
     * @param e                 Other entity being associated with
     * @return true if successful, false if not
     */
    public boolean add(GameUser user, String associationType, Entity e) {
        return add(user, associationType, e, Double.NaN, null);
    }

    /** Add a named association between a user and a value
     * @param user              Key user with the association
     * @param associationType   Tag for the association
     * @param val               Number to be associated with the user
     * @return true if successful, false if not
     */
    public boolean add(GameUser user, String associationType, double val) {
        return add(user, associationType, null, val, null);
    }

    /** Add a named association between a user and a string (up to 80 characters)
     * @param user              Key user with the association
     * @param associationType   Tag for the association
     * @param info              String to be associated with the user
     * @return true if successful, false if not
     */
    public boolean add(GameUser user, String associationType, String info) {
        return add(user, associationType, null, Double.NaN, info);
    }

    /** Add a named association between a user and nothing
     * @param user              Key user with the association
     * @param associationType   Tag for the association
     * @return true if successful, false if not
     */
    public boolean add(GameUser user, String associationType) {
        return add(user, associationType, null, Double.NaN, null);
    }

    /** Add a named association between a user and some other values
     * @param user              Key user with the association
     * @param associationType   Tag for the association
     * @param e                 Other entity being associated with (null if none)
     * @param val               Number to be associated with the user (Double.Nan if none)
     * @param info              String to be associated with the user (null if none)
     * @return true if successful, false if not
     */
    public boolean add(GameUser user, String associationType, Entity e, double val, String info) {
        UserAssociation ua = insert(new UserAssociationRecord(user.getId(), associationType,
                                    (e == null? EnumeratedRecord.noID : e.getId()), val, info));
        if (ua != null){
            add(ua, associationType);
            return true;
        }
        return false;
    }

    /** Remove the association the given user has with the given tag name
     * @param user              User with the association
     * @param associationType   Tag for the association being removed
     * @return true if found and successfully removed, false otherwise
     */
    public boolean remove(GameUser user, String associationType) {
        if (delete(user.getId(), associationType)) {
            associations.get(associationType).remove(user.getId());
            if (associations.get(associationType).size() <= 0) {
                associations.remove(associationType);
            }
            return true;
        }
        return false;
    }

    /** Returns a collection of all user associations with the given tag
     * @param associationType   Tag for the association "type"
     * @return  Collection of UserAssociations with that tag, or an empty collection if tag not present
     */
    public Collection<UserAssociation> get(String associationType) {
        HashMap<Integer, UserAssociation> map = associations.get(associationType);
        if (map == null)
            return new ArrayList<>();
        return map.values();
    }

    /** Returns the unique association of the given user an the given tag
     * @param user              User with the association
     * @param associationType   Tag or "type" of the association
     * @return full UserAssocation information for the association, or null if it doesn't exist
     */
    public UserAssociation get(GameUser user, String associationType) {
        HashMap<Integer, UserAssociation> map = associations.get(associationType);
        if (map == null)
            return null;
        return map.get(user.getId());
    }

    /** Returns all associations
     * @return a nested map of all associations, indexed first by tag, then by User ID
     */
    public Map<String, ? extends Map<Integer, UserAssociation>> getAll() {
        return associations;
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /** Adds the given userAssociation under the given tag in the associations map
     *
     * @param ua                UserAssocation to add
     * @param associationType   Tag to add it under
     */
    void add(UserAssociation ua, String associationType) {
        if (!associations.containsKey(associationType)) {
            associations.put(associationType, new HashMap<>());
        }
        associations.get(associationType).put(ua.user.getId(), ua);
    }

    /** inserts the passed record into the database and returns a new UserAssociation with
     * the appropriate data
     * @param record    Filled UserAssociationRecord with all the data need to add
     * @return A properly filled UserAssociation if successful, otherwise null.
     */
    UserAssociation insert(UserAssociationRecord record) {
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return null;

        try {
            // Create base item
            PreparedStatement insertStatement = record.prepareInsertStatement(dataConnection);
            if (insertStatement.executeUpdate() == 0) {
                dataConnection.close();
                return null;
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Error while adding user association.", e);
        }
        return new UserAssociation(userRepo, entityRepo, record);
    }

    /** deletes the record in the database with the passed user ID and associationType/tag
     * @param id                User ID of the association
     * @param associationType   Tag/type of the association
     * @return true if association was found and successfully deleted, false otherwise
     */
    boolean delete(int id, String associationType) {
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return false;

        try {
            // Create base item
            PreparedStatement insertStatement = dataConnection.prepareStatement(
                    " DELETE FROM UserAssociation WHERE " +
                    " User_EntityID=" + id + " AND " +
                    " Tag='" + associationType + "'; ");
            if (insertStatement.executeUpdate() == 0) {
                dataConnection.close();
                return false;
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Error while deleting user association.", e);
        }
        return true;
    }

    /**
     * Reads the database and fills the HashMaps as appropriate. Intended to be called once
     * at time of initialization.
     *
     * @param bzData    reference to BulletZoneData for making further SQL queries
     * @param repos     List of repositories of entities that can be owned
     * @param gameUserRepo repository of users that can have associations
     */
    public void refresh(BulletZoneData bzData, List<? extends EntityRepository> repos, GameUserRepository gameUserRepo) {
        entityRepo = new AggregateRepository(repos);
        userRepo = gameUserRepo;
        data = bzData;
        associations.clear();

        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return;
        try {
            Statement statement = dataConnection.createStatement();

            // Read mapping of users to items that they own
            ResultSet mappingResult = statement.executeQuery("SELECT * FROM UserAssociation");
            while (mappingResult.next()) {
                UserAssociationRecord record = new UserAssociationRecord(mappingResult);
                UserAssociation ua = new UserAssociation(userRepo, entityRepo, record);
                add(ua, record.tag);
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static info!", e);
        }
    }
}
