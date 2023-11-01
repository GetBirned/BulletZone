package edu.unh.cs.cs619.bulletzone.repository;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Stack;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.GridEvent;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import jdk.internal.net.http.common.Pair;

public class ActionCommandInvoker {
    private Stack<GridEvent> history = new Stack<>();
    public boolean executeCommand(long tankId, Command command) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Boolean res = command.execute();
        if (res) {
            //history.push(new GridEvent(tankId + " - " + command.getCommandType(), new Timestamp(System.currentTimeMillis())));
            GridEvent hist_event = new GridEvent(command.getCommandType(), new Timestamp(System.currentTimeMillis()));
            history.push(hist_event);
        }
       // System.out.println("Added " + command.getCommandType() + " command!");
        return res;
    }

    // Add methods to undo and redo commands if needed
    public void undoLastCommand() {
        if (!history.isEmpty()) {
            GridEvent lastCommand = history.pop();
            //lastCommand.undo();
        }
    }

    public LinkedList<GridEvent> getHistory(Timestamp timestamp) {
        LinkedList<GridEvent> l = new LinkedList<>();
        for (int i = 0; i < history.capacity(); i++) {
            if (history.get(i).getTimestamp().after(timestamp)) {
                break;
            } else {
                l.add(history.get(i));
            }
        }

        return l;
    }

    public Stack<GridEvent> getCommandHistory() {
        return history;
    }
}
