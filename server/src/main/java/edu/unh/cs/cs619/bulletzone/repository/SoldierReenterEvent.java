package edu.unh.cs.cs619.bulletzone.repository;

public class SoldierReenterEvent {
    private final long soldierId;

    public SoldierReenterEvent(long soldierId) {
        this.soldierId = soldierId;
    }

    public long getSoldierId() {
        return soldierId;
    }
}
