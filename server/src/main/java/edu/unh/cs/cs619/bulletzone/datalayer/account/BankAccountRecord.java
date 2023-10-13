package edu.unh.cs.cs619.bulletzone.datalayer.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

public class BankAccountRecord extends EntityRecord {
    double credits;

    BankAccountRecord() {
        super(EntityType.BankAccount);
        credits = 0;
    }

    BankAccountRecord(ResultSet itemResult){
        super(itemResult);
        try {
            credits = itemResult.getDouble("Credits");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from bank account result set", e);
        }
    }

    String getInsertString() {
        return " INSERT INTO BankAccount ( EntityID, Credits )\n" +
                "    VALUES (" + getID() + ", " + credits + "); ";
    }

    @Override
    public void insertInto(Connection dataConnection) throws SQLException {
        super.insertInto(dataConnection);
        PreparedStatement accountStatement = dataConnection.prepareStatement(getInsertString());

        int affectedRows = accountStatement.executeUpdate();
        if (affectedRows == 0)
            throw new SQLException("Creating BankAccount record failed.");
    }

    static boolean update(Connection dataConnection, int accountID, double amount) throws SQLException {
        PreparedStatement updateStatement = dataConnection.prepareStatement(
                "UPDATE BankAccount SET Credits=" + amount + " WHERE EntityID=" + accountID + "; ");
        if (updateStatement.executeUpdate() == 0) {
            dataConnection.close();
            return false; //nothing deleted
        }
        return true;
    }
}
