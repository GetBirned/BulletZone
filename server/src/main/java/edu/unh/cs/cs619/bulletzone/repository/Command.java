package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public interface Command {
    boolean execute() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException;

    String getCommandType();
}
