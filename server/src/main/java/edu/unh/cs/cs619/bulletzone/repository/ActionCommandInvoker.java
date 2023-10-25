package edu.unh.cs.cs619.bulletzone.repository;

import java.util.Stack;

import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class ActionCommandInvoker {
    private Stack<Command> history = new Stack<>();

    public boolean executeCommand(Command command) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Boolean res = command.execute();
        history.push(command);
        System.out.println("Added " + command.getCommandType() + " command!");
        return res;
    }

    // Add methods to undo and redo commands if needed
    public void undoLastCommand() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            //lastCommand.undo();
        }
    }
}
