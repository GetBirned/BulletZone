package edu.unh.cs.cs619.bulletzone.model;

import java.sql.Timestamp;

public class GridEvent {
    private Timestamp timestamp;
    private String command;
    public GridEvent(String command, Timestamp timestamp) {
        this.command = command;
        this.timestamp = timestamp;
    }

    public String getCommand() {return command;}
    public Timestamp getTimestamp() {return timestamp;}
}
