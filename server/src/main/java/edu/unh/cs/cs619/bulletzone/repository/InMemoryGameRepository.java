package edu.unh.cs.cs619.bulletzone.repository;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.GridEvent;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

@Component
public class InMemoryGameRepository implements GameRepository {

    /**
     * Field dimensions
     */
    private static final int FIELD_DIM = 16;


    /**
     * Bullet's impact effect [life]
     */
    private static final int BULLET_DAMAGE = 1;

    /**
     * Tank's default life [life]
     */
    private static final int TANK_LIFE = 100;
    private final AtomicLong idGenerator = new AtomicLong();
    private final Object monitor = new Object();
    private Game game = null;
    private Action action;
    private ActionCommandInvoker aci;

    @Override
    public Tank join(String ip) {
        synchronized (this.monitor) {
            Tank tank;
            if (game == null) {
                game = new Game();
            }

            if( (tank = game.getTank(ip)) != null){
                return tank;
            }

            Long tankId = this.idGenerator.getAndIncrement();

            tank = new Tank(tankId, Direction.Up, ip, 1);
            tank.setLife(TANK_LIFE);

            Random random = new Random();
            int x;
            int y;

            // This may run for forever.. If there is no free space. XXX
            for (; ; ) {
                x = random.nextInt(FIELD_DIM);
                y = random.nextInt(FIELD_DIM);
                FieldHolder fieldElement = game.getHolderGrid().get(x * FIELD_DIM + y);
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(tank);
                    tank.setParent(fieldElement);
                    break;
                }
            }

            game.addTank(ip, tank);

            /*
            I have abstracted this into the Action class
            - I think I need to make this a command pattern

            Join will be called first
             */

            // SETH
            // creating a new action and invoker class
            action = new Action(monitor, game); // Watch placement
            aci = new ActionCommandInvoker();

            return tank;
        }
    }



    @Override
    public int[][] getGrid() {
        synchronized (this.monitor) {
            if (game == null) {
                game = new Game();
            }
        }
        return game.getGrid2D();
    }

    @Override
    public boolean turn(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        // calling our new Action class
        //boolean res = action.turn(tankId, direction);
        Command turn_me = new ConcreteTurnCommand(action, tankId, direction);
        return aci.executeCommand(tankId, turn_me);
    }

    @Override
    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        //boolean res = action.move(tankId, direction);
        Command move_me = new ConcreteMoveCommand(action, tankId, direction);
        return aci.executeCommand(tankId, move_me);
    }


    @Override
    public boolean fire(long tankId, int bulletType)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        //boolean res = action.fire(tankId, bulletType);
        Command fire_me = new ConcreteFireCommand(action, tankId, bulletType);
        return aci.executeCommand(tankId, fire_me);
    }

    @Override
    public void leave(long tankId)
            throws TankDoesNotExistException {
        synchronized (this.monitor) {
            if (!this.game.getTanks().containsKey(tankId)) {
                throw new TankDoesNotExistException(tankId);
            }

            System.out.println("leave() called, tank ID: " + tankId);

            Tank tank = game.getTanks().get(tankId);
            FieldHolder parent = tank.getParent();
            parent.clearField();
            game.removeTank(tankId);
        }
    }

    public LinkedList<GridEvent> getHistory(Timestamp timestamp) {
        return aci.getHistory(timestamp);
    }

    public Game getGame() {
        return this.game;
    }
    @Override
    public void updateLife(long tankId, boolean isTank, long offset) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        game.setHealth(tankId,isTank,offset);
    }

    @Override
    public int getHealth(long tankId) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Command getHealth = new ConcreteGetHealthCommand(action, tankId);
        //aci.executeCommand(tankId, getHealth);
        int res = getHealth.execute1();

        return res;
    }

    @Override
    public int getSoldierHealth(long soldierId) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Command getSoldierHealth = new ConcreteGetSoldierHealthCommand(action, soldierId);

        int res = getSoldierHealth.execute1();

        return res;
    }


    @Override
    public void setTankPowerup(long tankId, int powerupValue) {
        game.setTankPowerup(tankId,powerupValue);
    }

    @Override
    public void setSoldierPowerup(long tankId, int powerupValue) {
        game.setSoldierPowerup(tankId,powerupValue);
    }


    @Override
    public int getSoldierPowerup(long tankId) {
        return game.getSoldierPowerup(tankId);
    }

    @Override
    public int getTankPowerup(long tankId) {
        return game.getTankPowerup(tankId);
    }

    public Stack<GridEvent> getCommandHistory() {
        return aci.getCommandHistory();
    }

    @Override
    public LongWrapper deploySoldier(long tankID) {
        return game.deploySoldier(tankID);
    }
}

