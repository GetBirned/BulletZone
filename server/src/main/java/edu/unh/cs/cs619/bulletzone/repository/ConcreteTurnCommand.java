package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class ConcreteTurnCommand implements Command {
    private Action action;
    private Direction oldDir;
    private Direction newDir;
    private long oldTankID;
    private long newTankID;


    /*
    oki so p much I need a "piece" to pass in
     */

    public ConcreteTurnCommand(Action action, long tankID, Direction dir) {
        this.newTankID = tankID;
        this.newDir = dir;
        this.action = action;
    }

    @Override
    public boolean execute() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        // TODO: implement fire, turn (only move rn)
        this.oldDir = null; // NEED TO USE A GET METHOD
        this.oldTankID = -1; // NEED TO USE A GET METHOD

        // NEED TO REVIEW WHAT OLD/NEW ID MEANS
        boolean res_t = turnConstraintCheck();
        if (res_t) {
            action.turn(newTankID, newDir);
            return true;
        } else {
            System.out.println("Not a valid turn");
            return false;
        }
    }

    public Boolean turnConstraintCheck() {
        /*
        Tank can make only one turn per step. (E.g., Correct turn: NORTH -> WEST Incorrect turn: NORTH -> SOUTH);
         */
        return true;
    }

    public void undo() {
        // NEED TO IMPLEMENT
    }

    @Override
    public String getCommandType() {
        return "Turn";
    }
}
