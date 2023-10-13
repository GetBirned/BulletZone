package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

class GameUserRecord extends EntityRecord {
    String name;
    String username;
    byte[] passwordHash;
    byte[] passwordSalt;

    GameUserRecord(String guName, String guUsername) {
        super(EntityType.User);
        name = guName;
        username = guUsername;
    }

    GameUserRecord(ResultSet userResult) {
        super(userResult);
        try {
            //ResultSetMetaData rsmd = userResult.getMetaData();
            //String firstColumnName = rsmd.getColumnName(1);
            //System.out.println(firstColumnName);
            name = userResult.getString("Name");
            username = userResult.getString("Username");
            passwordHash = decocdeBytesAsHex(userResult.getString("PasswordHash"));
            passwordSalt = decocdeBytesAsHex(userResult.getString("PasswordSalt"));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from user result set", e);
        }
    }

    @Override
    public void insertInto(Connection dataConnection) throws SQLException {
        super.insertInto(dataConnection);
        PreparedStatement userStatement = prepareInsertStatement(dataConnection);
        int affectedRows = userStatement.executeUpdate();
        if (affectedRows == 0)
            throw new SQLException("Creating User " + username + " failed.");
    }

    PreparedStatement prepareInsertStatement(Connection dataConnection) throws SQLException {
        PreparedStatement insertStatement = dataConnection.prepareStatement(getInsertString());
        insertStatement.setString(1, name);
        insertStatement.setString(2, username);
        insertStatement.setString(3, encodeBytesAsHex(passwordHash));
        insertStatement.setString(4, encodeBytesAsHex(passwordSalt));
        return insertStatement;
    }

    String getInsertString() {
        return " INSERT INTO User ( EntityID, Name, Username, PasswordHash, PasswordSalt )\n" +
                "    VALUES ('" + getID() + "', ?, ?, ?, ?); ";
    }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Converts a byte array to a string of uppercase hexadecimal numbers with no spaces
     * @param bytes The byte array to be encoded
     * @return  A string of upper-case hexadecimal characters without spaces between them
     */
    private String encodeBytesAsHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    /**
     * Converts a hexadecimal string without spaces to a sequence of bytes
     * @param hex   A string of hexadecimal characters without spaces between them
     * @return  The corresponding byte array
     */
    private byte[] decocdeBytesAsHex(String hex) {
        int length = hex.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) +
                    (Character.digit(hex.charAt(i+1), 16)));
        }
        return result;
    }
}
