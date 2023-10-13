/**
 * Enumeration for the permissions users can have for of items, and bank accounts (or anything
 * else that can be accessed by a user)
 */
package edu.unh.cs.cs619.bulletzone.datalayer.permission;

import java.util.HashMap;
import java.util.Map;

public enum Permission {
    Invalid, Owner, Add, Remove, Use, Transfer;

    //Thanks to Jason Waleryszak for initial version of the convenience methods below

    /**
     * Get all permissions. Useful for replying this structure using json.
     *
     * @return Map of Permission id-Permission name pairs.
     */
    public static Map<Integer, String> getPermissionMapping() { return Helper.permissions; }

    /**
     * Get a Permission given its integer ID.
     *
     * @param permissionID ID of Permission.
     * @return Permission associated with the ID. Permission.Invalid is returned for values
     * out-of-range
     */
    public static Permission get(int permissionID) {
        if (permissionID < 0 || permissionID >= values().length)
            return Invalid;

        return values()[permissionID];
    }

    /** Static method that gets the ID of the passed permission
     * @param p     Permission to get the ID for
     * @return      the integer ID of the passed permission
     */
    public static int getID(Permission p) {
        return p.ordinal();
    }

    /**
     * Get a Permission given its name.
     *
     * @param permissionName Name of Permission.
     * @return Permission associated with the name. Permission.Invalid returned if no permission
     * name matches.
     */
    public static Permission get(String permissionName) {
        Permission result;

        try {
            result = Permission.valueOf(permissionName);
        } catch (IllegalArgumentException e) {
            result = Invalid;
        }
        return result;
    }

    //using a static class to get around the prohibition on touching static member from constructor
    private static class Helper {
        public static Map<Integer, String> permissions = new HashMap<>();
    }

    private Permission() {
        Helper.permissions.put(ordinal(), name());
    }
}
