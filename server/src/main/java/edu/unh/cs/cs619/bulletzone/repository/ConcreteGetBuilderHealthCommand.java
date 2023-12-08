package edu.unh.cs.cs619.bulletzone.repository;

public class ConcreteGetBuilderHealthCommand implements Command {
    private final Action action;
    private final long tankId;

    public ConcreteGetBuilderHealthCommand(Action action, long tankId) {
        this.action = action;
        this.tankId = tankId;
    }

    @Override
    public boolean execute() {
        action.getBuilderHealth(tankId);
        return true;
    }

    @Override
    public int execute1() {
        return action.getBuilderHealth(tankId);
    }

    @Override
    public String getCommandType() {
        return "Get Builder Health";
    }

}
