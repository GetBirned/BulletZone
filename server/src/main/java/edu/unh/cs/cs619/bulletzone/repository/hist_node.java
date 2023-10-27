package edu.unh.cs.cs619.bulletzone.repository;

import java.sql.Timestamp;

public class hist_node {
    Timestamp ts;
    long tankId;
    Command command;

    public hist_node(Timestamp ts, long tankId, Command command) {
        this.ts = ts;
        this.tankId = tankId;
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public long getTankId() {
        return tankId;
    }

    public Timestamp getTimestamp() {
        return ts;
    }
}
