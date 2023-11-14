package edu.unh.cs.cs619.bulletzone.repository;

public class ConcreteGetHealthCommand implements Command {

    private final Action action;
    private final long tankId;

    public ConcreteGetHealthCommand(Action action, long tankId) {
        this.action = action;
        this.tankId = tankId;
    }

    @Override
    public boolean execute() {
        action.getHealth(tankId);
        return true; // You may need to modify this based on your logic
    }

    @Override
    public int execute1() {
        return action.getHealth(tankId);
    }

    @Override
    public String getCommandType() {
        return "Get Health";
    }
}
