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

        public Timestamp getTs() {
            return ts;
        }
    }
    private Stack<hist_node> history = new Stack<>();
    public boolean executeCommand(long tankId, Command command) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Boolean res = command.execute();
        if (res) {
            //history.push(new GridEvent(tankId + " - " + command.getCommandType(), new Timestamp(System.currentTimeMillis())));
            hist_node hist_event = new hist_node(new Timestamp(System.currentTimeMillis()), tankId, command);
            history.push(hist_event);
        }
       // System.out.println("Added " + command.getCommandType() + " command!");
        return res;
    }

    // Add methods to undo and redo commands if needed
    public void undoLastCommand() {
        if (!history.isEmpty()) {
            hist_node lastCommand = history.pop();
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
