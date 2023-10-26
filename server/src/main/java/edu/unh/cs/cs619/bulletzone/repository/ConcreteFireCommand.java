package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class ConcreteFireCommand implements Command {
    private Action action;
    private int bt;
    private long oldTankID;
    private long newTankID;


    /*
    oki so p much I need a "piece" to pass in
     */

    public ConcreteFireCommand(Action action, long tankID, int bullet_type) {
        this.newTankID = tankID;
        this.bt = bullet_type;
        this.action = action;
    }

    @Override
    public boolean execute() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        // TODO: implement fire, turn (only move rn)
        this.bt = -1; // NEED TO USE A GET METHOD
        this.oldTankID = -1; // NEED TO USE A GET METHOD

        // NEED TO REVIEW WHAT OLD/NEW ID MEANS
        return action.fire(newTankID, bt);
    }

    @Override
    public String getCommandType() {
        return "Fire";
    }

    public void undo() {
        // NEED TO IMPLEMENT
    }
}
