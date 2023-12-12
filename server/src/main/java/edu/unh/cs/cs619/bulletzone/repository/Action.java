package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Timer;

import edu.unh.cs.cs619.bulletzone.model.Bridge;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Forest;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Grass;
import edu.unh.cs.cs619.bulletzone.model.HealthKit;
import edu.unh.cs.cs619.bulletzone.model.Hill;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Road;
import edu.unh.cs.cs619.bulletzone.model.Rocky;
import edu.unh.cs.cs619.bulletzone.model.Shield;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.TankLocation;
import edu.unh.cs.cs619.bulletzone.model.Thingamajig;
import edu.unh.cs.cs619.bulletzone.model.Vehicle;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.Water;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;

/*
    I believe this should ultimately be a command? pattern
    new action class
 */
public class Action {
    private final Object monitor;
    private Game game;
    private final Timer timer = new Timer();

    private int bulletDamage[]={10,30,50};
    private int bulletDelay[]={500,1000,1500};
    private int trackActiveBullets[]={0,0};

    /**
     * Bullet step time in milliseconds
     */
    private static final int BULLET_PERIOD = 200;



    public Action(Object monitor, Game game) {
        this.monitor = monitor;
        this.game = game;
    }
    public boolean turn(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            checkNotNull(direction);
            Builder builder = game.getBuilders().get(tankId);

            // Find user
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                throw new TankDoesNotExistException(tankId);
            }
            if (tank.getIsActive() == 1) {
                return turnHelper(tank,  direction);
            } else if (builder.getIsActive() == 1) {
                return turnHelper(builder,  direction);
            } else {
                Soldier soldier = game.getSoldiers().get(tankId);
                return turnHelper(soldier, direction);
            }
        }
    }

    private boolean turnHelper(Vehicle v, Direction direction) {
        long millis = System.currentTimeMillis();

        if(millis < v.getLastMoveTime()) {
            return false;
        }
        v.setLastMoveTime(millis+v.getAllowedMoveInterval());
        v.setDirection(direction);

        return true;
    }

    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            Tank tank = game.getTanks().get(tankId);
            Builder builder = game.getBuilders().get(tankId);
            if (tank == null) {
                throw new TankDoesNotExistException(tankId);
            }

            if (tank.getIsActive() == 1) {
                //if tank direction is not equal to forwards or backwards
                //move constraint
                return moveHelper(tank, direction, tank, tankId);
            } else if (builder.getIsActive() == 1) {
                return moveHelper(builder, direction, tank, tankId);
            } else {
                Soldier soldier = game.getSoldiers().get(tankId);
                return moveHelper(soldier, direction, tank, tankId);

            }
        }
    }

    private boolean moveHelper(Vehicle v, Direction direction, Tank tank, long tankId) {
        if (Direction.toByte(direction) != Direction.toByte(v.getDirection()) && Direction.toByte(direction) != Direction.opposite(v.getDirection())) {
            return false;
        }


        long millis = System.currentTimeMillis();
        if(millis < v.getLastMoveTime())
            return false;

        v.setLastMoveTime(millis + v.getAllowedMoveInterval());

        FieldHolder parent = v.getParent();
        FieldHolder nextField = parent.getNeighbor(direction);
        checkNotNull(parent.getNeighbor(direction), "Neighbor is not available");

        boolean isCompleted;

        /** need to fix so that soldier can go into forest**/

        if (!nextField.isPresent() || nextField.getEntity() instanceof Hill || nextField.getEntity() instanceof Rocky
                || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp || nextField.getEntity() instanceof nukePowerUp
                || nextField.getEntity() instanceof Shield || nextField.getEntity() instanceof HealthKit
                || nextField.getEntity() instanceof Grass || nextField.getEntity() instanceof Road ||
                nextField.getEntity() instanceof Bridge || nextField.getEntity() instanceof Forest ) {
            // If the next field is empty move the user
            int walkedOnRoad = 0;
            FieldHolder behindVehicle = v.getParent().getNeighbor(getOffsetForDirection(v.getDirection()));


            //Constraint to allow tanks on hills and rocky terrain and to slow them on hills
            if (nextField.isPresent()) {
                if (nextField.getEntity() instanceof Hill) {
                    if (!(parent.getEntity() instanceof Hill)) {
                        v.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() * 1.5));
                    }
                } else if (nextField.getEntity() instanceof Road) {
                    // Road logic : move speed to entire halved
                    walkedOnRoad = 1;
                    nextField.setFieldEntity(new Road());
                    if (!((parent.getEntity()) instanceof Hill)) {
                        v.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 2));
                    }
                }
            } else {
                if (parent.getEntity() instanceof Hill) {
                    v.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 1.5));
                }
            }

            int roadBehind, bridgeBehind = 0;
            if (behindVehicle.getEntity() instanceof Bridge) {
                bridgeBehind = 1;
            } else if (behindVehicle.getEntity() instanceof Road) {
                roadBehind = 1;
            }

            parent.clearField();

            if(v.getPowerUpType() == 4) {
                System.out.println("Restoring terrain. Current entity type: hill");
                parent.setFieldEntity(new Hill());
            } else if(v.getPowerUpType() == 5) {
                System.out.println("Restoring terrain. Current entity type: rock");
                parent.setFieldEntity(new Rocky());
            } else if(v.getPowerUpType() == 6) {
                System.out.println("Restoring terrain. Current entity type: forest");
                parent.setFieldEntity(new Forest());
            } else if (v.getPowerUpType() == 8) {
                System.out.println("Restoring terrain. Current entity type: water");
                parent.setFieldEntity(new Water());
            } else if (v.getPowerUpType() == 11) {
                System.out.println("Restoring terrain. Current entity type: bridge");
                parent.setFieldEntity(new Bridge());
            } else if (v.getPowerUpType() == 12) {
                System.out.println("Restoring terrain. Current entity type: road");
                parent.setFieldEntity(new Road());
            }
            nextField.setFieldEntity((FieldEntity) v);
            v.setParent(nextField);

            isCompleted = true;
        } else if (nextField.getEntity() instanceof Tank && v instanceof Soldier) {
            Soldier soldier = (Soldier) v;
            Tank t = (Tank) nextField.getEntity();
            if (t.getId() == soldier.getId()) { // make sure soldier is attempting to join it's own tank
                if (t.getLife() != 0) { // cannot enter tank if its dead
                    if (soldier.reenterTank(tank)) {
                        game.removeSoldier(tankId);
                        game.startEjectionCooldown();
                        soldier.getParent().clearField();
                        isCompleted = true;
                    } else {
                        // Re-entry failed, soldier is already in a tank
                        isCompleted = false;
                    }
                }
            } else {
                return true;
            }
        } else if (nextField.getEntity() instanceof Water && v instanceof Builder) {
            Builder b = (Builder) v;
            if (nextField.getEntity() instanceof Water) {
                FieldHolder behindBuilder = v.getParent().getNeighbor(getOffsetForDirection(v.getDirection()));
                if (parent.getEntity() instanceof Water) {
                    v.setAllowedMoveInterval((int) (250));
                    nextField.setFieldEntity(new Water());
                    behindBuilder.setFieldEntity(new Water());
                } else if (!(parent.getEntity() instanceof Water) && parent.getEntity() instanceof Water) {
                    v.setAllowedMoveInterval((int) (b.getAllowedMoveInterval() * 2));
                    parent.clearField();
                }
            }

            parent.clearField();
            nextField.setFieldEntity((FieldEntity) v);
            v.setParent(nextField);
            return true;
        } else if (nextField.getEntity() instanceof Wall && v instanceof Builder) {
            Wall wall = (Wall) nextField.getEntity();
            Builder b = (Builder) v;
            double damageToWall = Math.ceil(v.getLife() * 0.05);
            double damageToBuilder = Math.floor(wall.destructValue * 0.1);
            b.hit((int)damageToBuilder);
            wall.takeDamage((int)damageToWall);
            if (b.getLife() < 0) {
                b.getParent().clearField();
                b.setParent(null);
                game.removeBuilder(b.getId());
            }
            return false;
        } else if (nextField.getEntity() instanceof Soldier && v instanceof Builder) {
            Soldier s = (Soldier) nextField.getEntity();
            Builder b = (Builder) v;
            double damageToSoldier = Math.ceil(v.getLife() * 0.05);
            double damageToBuilder = Math.floor(s.getLife() * 0.1);
            b.hit((int)damageToBuilder);
            s.hit((int) damageToSoldier);
            if (b.getLife() < 0) {
                b.getParent().clearField();
                b.setParent(null);
                game.removeBuilder(b.getId());
            }
            if (s.getLife() < 0) {
                s.getParent().clearField();
                s.setParent(null);
                game.removeSoldier(s.getId());
            }
            return false;
        } else if(nextField.getEntity() instanceof Tank && v instanceof Builder) {
            Tank t = (Tank) nextField.getEntity();
            Builder b = (Builder) v;
            double damageToTank = Math.ceil(b.getLife() * 0.05);
            double damageToBuilder = Math.floor(t.getLife() * 0.1);
            b.hit((int)damageToBuilder);
            t.hit((int)damageToTank);
            if (b.getLife() < 0) {
                b.getParent().clearField();
                b.setParent(null);
                game.removeBuilder(b.getId());
            }
            if (t.getLife() < 0) {
                t.getParent().clearField();
                t.setParent(null);
                game.removeTank(t.getId());
            }
            return false;
        } else {
            if (nextField.getEntity() instanceof Wall) {
                if(((Wall) nextField.getEntity()).destructValue == 1000){
                    v.takeDamage(10);
                    ((Wall) nextField.getEntity()).takeDamage(v.getLife());
                }
                else {
                    v.takeDamage(10);
                    ((Wall) nextField.getEntity()).takeDamage(v.getLife());
                }
            }
            if (nextField.getEntity() instanceof Tank) {
                v.takeDamage(((Tank) nextField.getEntity()).getLife());
                ((Tank) nextField.getEntity()).takeDamage(v.getLife());
            }
            isCompleted = false;
        }
        updateLocation(v, Direction.toByte(direction));
        return true;
    }
    private void updateLocation(Vehicle v, byte direction) {
        TankLocation tl = v.getTankLocation();
        if (direction == 0) {
            if (tl.getRow() == 0) {
                v.setTankLocation(new TankLocation(15, tl.getColumn()));
            } else {
                v.setTankLocation(new TankLocation(tl.getRow() - 1, tl.getColumn()));
            }
        } else if (direction == 2) {
            if (tl.getColumn() == 15) {
                v.setTankLocation(new TankLocation(tl.getRow(), 0));
            } else {
                v.setTankLocation(new TankLocation(tl.getRow(), tl.getColumn() + 1));
            }
        } else if (direction == 4) {
            if (tl.getRow() == 15) {
                v.setTankLocation(new TankLocation(0, tl.getColumn()));
            } else {
                v.setTankLocation(new TankLocation(tl.getRow() + 1, tl.getColumn()));
            }
        } else if (direction == 6) {
            if (tl.getColumn() == 0) {
                v.setTankLocation(new TankLocation(tl.getRow(), 15));
            } else {
                v.setTankLocation(new TankLocation(tl.getRow(), tl.getColumn() - 1));
            }
        }
    }

    public boolean fire(long tankId, int bulletType)
            throws TankDoesNotExistException, LimitExceededException {
        synchronized (this.monitor) {

            // Find tank
            Tank tank = game.getTanks().get(tankId);
            Builder builder = game.getBuilders().get(tankId);
            if (tank == null) {
                //Log.i(TAG, "Cannot find user with id: " + tankId);
                //return false;
                throw new TankDoesNotExistException(tankId);
            }
            Bullet bullet = null;
            if (tank.getIsActive() == 1) {
                bullet = bulletSetup(tank, bulletType, tankId);
                if (bullet == null) {
                    return false;
                }
                BulletMover bulletMover = new BulletMover();
                bulletMover.fireHelper(tank, bullet, monitor, game, trackActiveBullets);
            } else if (builder.getIsActive() == 1) { // Builder fire
                Soldier soldier = game.getSoldiers().get(tankId);
                bullet = bulletSetup(builder, bulletType, tankId);
                if (bullet == null) {
                    return false;
                }
                BulletMover bulletMover = new BulletMover();
                bulletMover.fireHelper(builder, bullet, monitor, game, trackActiveBullets);
            } else {
                Soldier soldier = game.getSoldiers().get(tankId);
                bullet = bulletSetup(soldier, bulletType, tankId);
                if (bullet == null) {
                    return false;
                }
                BulletMover bulletMover = new BulletMover();
                bulletMover.fireHelper(soldier, bullet, monitor, game, trackActiveBullets);
            }
        }
        return true;
    }

    private Bullet bulletSetup(Vehicle v, int bulletType, long tankId) {
        if (v.getNumberOfBullets() >= v.getAllowedNumberOfBullets())
            return null;

        long millis = System.currentTimeMillis();
        if (millis < v.getLastFireTime() && millis  > v.getAllowedFireInterval()) {
            return null;
        }
        Direction direction = v.getDirection();
        FieldHolder parent = v.getParent();
        v.setNumberOfBullets(v.getNumberOfBullets() + 1);

        if (!(bulletType >= 1 && bulletType <= 3)) {
            System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
            bulletType = 1;
        }

        v.setLastFireTime(millis + bulletDelay[bulletType - 1]);

        int bulletId = 0;
        if (trackActiveBullets[0] == 0) {
            bulletId = 0;
            trackActiveBullets[0] = 1;
        } else if (trackActiveBullets[1] == 0) {
            bulletId = 1;
            trackActiveBullets[1] = 1;
        }
        final Bullet bullet = new Bullet(tankId, direction, 30);
        // Set the same parent for the bullet.
        // This should be only a one way reference.
        bullet.setParent(parent);
        bullet.setBulletId(bulletId);
        return bullet;
    }
    public void updateLife(long tankId, int newLife) {
        // Find the tank with tankId and update its life
        Tank tank = game.getTanks().get(tankId);
        if (tank != null) {
            tank.setLife(newLife);
            // Add any additional logic needed, e.g., notifying other players
        }
    }

    public int getHealth(long tankId) {
        Tank tank = game.getTanks().get(tankId);
        if(tank != null) {
            return tank.getLife();
        }
        return 0;
    }

    public int getBuilderHealth(long tankId) {
        Builder builder = game.getBuilder(tankId);
        if(builder != null) {
            return builder.getLife();
        }
        return 0;
    }

    public int getSoldierHealth(long soldierId) {
        Soldier soldier = game.getSoldier((int)soldierId);
        if(soldier != null) {
            return soldier.getLife();
        }
        System.out.println("soldier is null");
        return 0;
    }

    private Direction getOffsetForDirection(Direction direction) {
        switch (direction) {
            case Up:
                return Direction.Down; // UP
            case Down:
                return Direction.Up; // DOWN
            case Left:
                return Direction.Right; // LEFT
            case Right:
                return Direction.Left; // RIGHT
            default:
                return null;
        }
    }

}
