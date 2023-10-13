package edu.unh.cs.cs619.bulletzone.datalayer.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EnumeratedRecord;

public class AccountTransferHistoryRecord extends EnumeratedRecord {
    int accountTransferHistoryID;
    public final int sourceBankAccountID;
    public final double sourceBalancePrior;
    public final int destBankAccountID;
    public final double destBalancePrior;
    public final double transferAmount;
    public final Timestamp timestamp;

    final static String recordName = "AccountTransferHistory";

    AccountTransferHistoryRecord(int accountID, double balance, double amountToAdd) {
        accountTransferHistoryID = noID;
        sourceBankAccountID = noID;
        sourceBalancePrior = Double.NaN;
        destBankAccountID = accountID;
        destBalancePrior = balance;
        transferAmount = amountToAdd;
        Date date = new Date();
        timestamp = new Timestamp(date.getTime());
    }

    AccountTransferHistoryRecord(int fromID, double fromBalance, int toID, double toBalance, double amount) {
        accountTransferHistoryID = noID;
        sourceBankAccountID = fromID;
        sourceBalancePrior = fromBalance;
        destBankAccountID = toID;
        destBalancePrior = toBalance;
        transferAmount = amount;
        Date date = new Date();
        timestamp = new Timestamp(date.getTime());
    }

    AccountTransferHistoryRecord(ResultSet itemResult){
        int tempID;
        double tempVal;
        try {
            accountTransferHistoryID = itemResult.getInt("AccountTransferHistoryID");
            tempID = itemResult.getInt("SourceBankAccountID");
            if (itemResult.wasNull())
                tempID = noID;
            sourceBankAccountID = tempID;
            tempVal = itemResult.getDouble("SourceBalancePrior");
            if (itemResult.wasNull())
                tempVal = Double.NaN;
            sourceBalancePrior = tempVal;
            destBankAccountID = itemResult.getInt("DestBankAccountID");
            destBalancePrior = itemResult.getDouble("DestBalancePrior");
            transferAmount = itemResult.getDouble("TransferAmount");
            timestamp = itemResult.getTimestamp("Timestamp");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to extract data from account transfer result set", e);
        }
    }

    @Override
    public int getID() { return accountTransferHistoryID; }

    @Override
    public void setID(int id) { accountTransferHistoryID = id; }

    @Override
    public String getRecordInsertString() {
        return " INSERT INTO " + recordName + " ( SourceBankAccountID, SourceBalancePrior, " +
                     "DestBankAccountID, DestBalancePrior, TransferAmount, Timestamp )\n" +
                "    VALUES (" + (sourceBankAccountID == noID? "null" : sourceBankAccountID) + ", " +
                                (Double.isNaN(sourceBalancePrior)? "null" : sourceBalancePrior) + ", " +
                                destBankAccountID + ", " +
                                destBalancePrior + ", " +
                                transferAmount + ", '" +
                                timestamp + "'); ";
    }

    void insertInto(Connection dataConnection) throws SQLException {
        insertInto(dataConnection, recordName);
    }
}
