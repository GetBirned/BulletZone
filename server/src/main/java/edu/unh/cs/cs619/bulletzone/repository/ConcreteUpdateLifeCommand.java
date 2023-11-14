package edu.unh.cs.cs619.bulletzone.repository;

public class ConcreteUpdateLifeCommand implements Command {

    private final Action action;
    private final long tankId;
    private final int newLife;

    public ConcreteUpdateLifeCommand(Action action, long tankId, int newLife) {
        this.action = action;
        this.tankId = tankId;
        this.newLife = newLife;
    }

    @Override
    public boolean execute() {
        action.updateLife(tankId, newLife);
        return true; // You may need to modify this based on your logic
    }

    @Override
    public int execute1() {
        return 0;
    }

    @Override
    public String getCommandType() {
        return "Update Life";
    }
}