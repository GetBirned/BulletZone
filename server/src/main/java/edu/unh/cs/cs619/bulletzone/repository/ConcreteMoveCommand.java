package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class ConcreteMoveCommand implements Command{
    private Action action;
    private Direction oldDir;
    private Direction newDir;
    private long oldTankID;
    private long newTankID;


    /*
    oki so p much I need a "piece" to pass in
     */

    public ConcreteMoveCommand(Action action, long tankID, Direction dir) {
        this.newTankID = tankID;
        this.newDir = dir;
        this.action = action;
    }

    @Override
    public boolean execute() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        // TODO: implement fire, turn (only move rn)
        this.oldDir = null; // NEED TO USE A GET METHOD
        this.oldTankID = -1; // NEED TO USE A GET METHOD

        boolean res_t = action.move(newTankID, newDir);
        if (!res_t) {
            // NEED TO REVIEW WHAT OLD/NEW ID MEANS
          //  System.out.println("Violation of move constraint check");
        }
        return res_t;
    }

    @Override
    public int execute1() {
        return 0;
    }

    public boolean moveConstraintCheck() {
        /*
        Tank can only move forward or back relative to its current direction. No sideways movements are allowed.
        Tank can move once in every X seconds. (X = 0.5);
         */
        return true;
    }

    public void undo() {
        // NEED TO IMPLEMENT
    }

    @Override
    public String getCommandType() {
        return "Move";
    }
}
