package edu.unh.cs.cs619.bulletzone.repository;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

import static com.google.common.base.Preconditions.checkNotNull;

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
                this.create();
            }

            if( (tank = game.getTank(ip)) != null){
                return tank;
            }

            Long tankId = this.idGenerator.getAndIncrement();

            tank = new Tank(tankId, Direction.Up, ip);
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
                this.create();
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
        return aci.executeCommand(turn_me);
    }

    @Override
    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        //boolean res = action.move(tankId, direction);
        Command move_me = new ConcreteMoveCommand(action, tankId, direction);
        return aci.executeCommand(move_me);
    }

    @Override
    public boolean fire(long tankId, int bulletType)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        //boolean res = action.fire(tankId, bulletType);
        Command fire_me = new ConcreteFireCommand(action, tankId, bulletType);
        return aci.executeCommand(fire_me);
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

    public void create() {
        if (game != null) {
            return;
        }
        synchronized (this.monitor) {

            this.game = new Game();

            createFieldHolderGrid(game);
            FieldEntities f = new FieldEntities();
            game = f.set(game);

        }
    }

    private void createFieldHolderGrid(Game game) {
        synchronized (this.monitor) {
            game.getHolderGrid().clear();
            for (int i = 0; i < FIELD_DIM * FIELD_DIM; i++) {
                game.getHolderGrid().add(new FieldHolder());
            }

            FieldHolder targetHolder;
            FieldHolder rightHolder;
            FieldHolder downHolder;

            // Build connections
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    targetHolder = game.getHolderGrid().get(i * FIELD_DIM + j);
                    rightHolder = game.getHolderGrid().get(i * FIELD_DIM
                            + ((j + 1) % FIELD_DIM));
                    downHolder = game.getHolderGrid().get(((i + 1) % FIELD_DIM)
                            * FIELD_DIM + j);

                    targetHolder.addNeighbor(Direction.Right, rightHolder);
                    rightHolder.addNeighbor(Direction.Left, targetHolder);

                    targetHolder.addNeighbor(Direction.Down, downHolder);
                    downHolder.addNeighbor(Direction.Up, targetHolder);
                }
            }
        }
    }

}
