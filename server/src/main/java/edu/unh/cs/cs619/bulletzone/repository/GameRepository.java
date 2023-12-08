package edu.unh.cs.cs619.bulletzone.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.GridEvent;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;


public interface GameRepository {

    Tank join(String ip);

    int[][] getGrid();

    boolean turn(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException;

    boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException;

    boolean fire(long tankId, int strength)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException;

    public void leave(long tankId)
            throws TankDoesNotExistException;

    public LinkedList<GridEvent> getHistory(Timestamp timestamp);

    void setSoldierPowerup(long tankId, int powerupValue);

    void setTankPowerup(long tankId, int powerupValue);

    int getSoldierPowerup(long tankId);

    int getTankPowerup(long tankId);

    public LongWrapper deploySoldier(long tankID);
    void updateLife(long tankId, boolean isTank, long offset) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;

    public int getHealth(long tankId) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;
    public int getSoldierHealth(long soldierId) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;

    public LongWrapper dismantleImprovement(long builderId);
    public LongWrapper buildImprovement(int choice, long builderId);
    public ArrayList<Integer> getSoldierPowerups(long soldierId);
    public ArrayList<Integer> getTankPowerups(long tankId);
    public void updateLife(long tankId, int newLife) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;
    public int getBuilderHealth(long tankId) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;
}
