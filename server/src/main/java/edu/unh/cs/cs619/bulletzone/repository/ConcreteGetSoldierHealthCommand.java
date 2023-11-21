package edu.unh.cs.cs619.bulletzone.repository;

public class ConcreteGetSoldierHealthCommand implements Command{

    private final Action action;
    private final long soldierId;

    public ConcreteGetSoldierHealthCommand(Action action, long soldierId) {
        this.action = action;
        this.soldierId = soldierId;
    }

    @Override
    public boolean execute() {
        action.getSoldierHealth(soldierId);
        return true;
    }

    @Override
    public int execute1() {
        return action.getSoldierHealth(soldierId);
    }

    @Override
    public String getCommandType() {
        return "Get Soldier Health";
    }
}
