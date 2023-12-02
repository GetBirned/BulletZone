package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bridge;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
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
import edu.unh.cs.cs619.bulletzone.model.Thingamajig;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.Water;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;

/*
    I believe this should ultimately be a command? pattern
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

            long millis = System.currentTimeMillis();
            if (tank.getIsActive() == 1) {
                if(millis < tank.getLastMoveTime())
                    return false;

                tank.setLastMoveTime(millis+tank.getAllowedMoveInterval());

                tank.setDirection(direction);

                return true;
            } else if (builder.getIsActive() == 1) {
                BuilderAction b = new BuilderAction(monitor, game);
                return b.turn(tankId, direction);
            } else {
                SoldierAction s = new SoldierAction(monitor, game);
                return s.turn(tankId, direction);
            }
        }
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
                if (Direction.toByte(direction) != Direction.toByte(tank.getDirection()) && Direction.toByte(direction) != Direction.opposite(tank.getDirection())) {
                    return false;
                }


                long millis = System.currentTimeMillis();
                if(millis < tank.getLastMoveTime())
                    return false;

                tank.setLastMoveTime(millis + tank.getAllowedMoveInterval());

                FieldHolder parent = tank.getParent();
                FieldHolder nextField = parent.getNeighbor(direction);
                checkNotNull(parent.getNeighbor(direction), "Neighbor is not available");

                boolean isCompleted;
                if (!nextField.isPresent() || nextField.getEntity() instanceof Hill || nextField.getEntity() instanceof Rocky
                        || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp || nextField.getEntity() instanceof nukePowerUp
                        || nextField.getEntity() instanceof Shield || nextField.getEntity() instanceof HealthKit
                        || nextField.getEntity() instanceof Grass || nextField.getEntity() instanceof Road || nextField.getEntity() instanceof Bridge) {
                    // If the next field is empty move the user



                    //Constraint to allow tanks on hills and rocky terrain and to slow them on hills
                    if (nextField.isPresent()) {
                        if (nextField.getEntity() instanceof Hill) {
                            if (!(parent.getEntity() instanceof Hill)) {
                                tank.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() * 1.5));
                            }
                        } else if (nextField.getEntity() instanceof Road) {
                            // Road logic : move speed to entire halved
                            if (!((parent.getEntity()) instanceof Hill)) {
                                tank.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 2));
                            }
                        }
                    } else {
                        if (parent.getEntity() instanceof Hill) {
                            tank.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 1.5));
                        }
                    }


                    parent.clearField();

                    if(tank.getPowerUpType() == 4) {
                        System.out.println("Restoring terrain. Current entity type: hill");
                        parent.setFieldEntity(new Hill());
                    } else if(tank.getPowerUpType() == 5) {
                        System.out.println("Restoring terrain. Current entity type: rock");
                        parent.setFieldEntity(new Rocky());
                    } else if (tank.getPowerUpType() == 8) {
                        System.out.println("Restoring terrain. Current entity type: water");
                        parent.setFieldEntity(new Water());
                    } else if (tank.getPowerUpType() == 9) {
                        System.out.println("Restoring terrain. Current entity type: road");
                        parent.setFieldEntity(new Road());
                    }
                    nextField.setFieldEntity(tank);
                    tank.setParent(nextField);

                    isCompleted = true;
                } else {
                    if (nextField.getEntity() instanceof Wall) {
                        if(((Wall) nextField.getEntity()).destructValue == 1000){
                            tank.takeDamage(10);
                            ((Wall) nextField.getEntity()).takeDamage(tank.getLife());
                        }
                        else {
                            tank.takeDamage(10);
                            ((Wall) nextField.getEntity()).takeDamage(tank.getLife());
                        }
                    }
                    if (nextField.getEntity() instanceof Tank) {
                        tank.takeDamage(((Tank) nextField.getEntity()).getLife());
                        ((Tank) nextField.getEntity()).takeDamage(tank.getLife());
                    }
                    isCompleted = false;
                }

                return isCompleted;
            } else if (builder.getIsActive() == 1) {
                BuilderAction b = new BuilderAction(monitor, game);
                return b.move(tankId, direction);
            } else {
                SoldierAction s = new SoldierAction(monitor, game);
                return s.move(tankId, direction);
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
            if (tank.getIsActive() == 1) {
                if (tank.getNumberOfBullets() >= tank.getAllowedNumberOfBullets())
                    return false;

                long millis = System.currentTimeMillis();
                if (millis < tank.getLastFireTime()/*>tank.getAllowedFireInterval()*/) {
                    return false;
                }

                //Log.i(TAG, "Cannot find user with id: " + tankId);
                Direction direction = tank.getDirection();
                FieldHolder parent = tank.getParent();
                tank.setNumberOfBullets(tank.getNumberOfBullets() + 1);

                if (!(bulletType >= 1 && bulletType <= 3)) {
                    System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
                    bulletType = 1;
                }

                tank.setLastFireTime(millis + bulletDelay[bulletType - 1]);

                int bulletId = 0;
                if (trackActiveBullets[0] == 0) {
                    bulletId = 0;
                    trackActiveBullets[0] = 1;
                } else if (trackActiveBullets[1] == 0) {
                    bulletId = 1;
                    trackActiveBullets[1] = 1;
                }

                // Create a new bullet to fire
                //CHANGING HERE **********************************************
                final Bullet bullet = new Bullet(tankId, direction, 30);
                // Set the same parent for the bullet.
                // This should be only a one way reference.
                bullet.setParent(parent);
                bullet.setBulletId(bulletId);

                // TODO make it nicer
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        synchronized (monitor) {
                            System.out.println("Active Bullet: " + tank.getNumberOfBullets() + "---- Bullet ID: " + bullet.getIntValue());
                            FieldHolder currentField = bullet.getParent();
                            Direction direction = bullet.getDirection();
                            FieldHolder nextField = currentField
                                    .getNeighbor(direction);

                            // Is the bullet visible on the field?
                            boolean isVisible = currentField.isPresent()
                                    && (currentField.getEntity() == bullet);


                            if (nextField.isPresent()  && !(nextField.getEntity() instanceof Hill) && !(nextField.getEntity() instanceof Rocky)
                                    && !(nextField.getEntity() instanceof Water) && !(nextField.getEntity() instanceof Road) && !(nextField.getEntity() instanceof Bridge)) {
                                // Something is there, hit it
                                nextField.getEntity().hit(bullet.getDamage());

                                if (nextField.getEntity() instanceof Tank) {
                                    Tank t = (Tank) nextField.getEntity();
                                    System.out.println("tank is hit, tank life: " + t.getLife());
                                    if (t.getLife() <= 0) {
                                        t.getParent().clearField();
                                        t.setParent(null);
                                        game.removeTank(t.getId());
                                    }
                                } else if (nextField.getEntity() instanceof Soldier) { // Soldier Hit
                                    Soldier s = (Soldier) nextField.getEntity();
                                    System.out.println("soldier is hit, soldier life: " + s.getLife());
                                    if (s.getLife() <= 0) {
                                        Tank t = game.getTank(s.getIp());
                                        t.getParent().clearField();
                                        t.setParent(null);
                                        s.getParent().clearField();
                                        s.setParent(null);
                                        game.removeSoldier(s.getId());
                                        game.removeTank(t.getId());
                                    }
                                } else if (nextField.getEntity() instanceof Builder) {
                                    Builder b = (Builder) nextField.getEntity();
                                    System.out.println("builder is hit, builder life: " + b.getLife());
                                    if (b.getLife() <= 0) {
                                        b.getParent().clearField();
                                        b.setParent(null);
                                        game.removeBuilder(b.getId());
                                    }
                                } else if (nextField.getEntity() instanceof Wall) {
                                    Wall w = (Wall) nextField.getEntity();
                                    if (w.getIntValue() > 1000 && w.getIntValue() <= 2000) {
                                        game.getHolderGrid().get(w.getPos()).clearField();
                                    }
                                } System.out.println("Before clearing field. Entity type: " + nextField.getEntity().getClass().getSimpleName());

                                if (nextField.getEntity() instanceof Shield || nextField.getEntity() instanceof HealthKit
                                        || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp
                                        || nextField.getEntity() instanceof nukePowerUp) {
                                    // Double-check that the nextField.getEntity() instance matches the actual class type of Shield or HealthKit
                                    System.out.println("Clearing field for Shield or HealthKit. Entity type: " + nextField.getEntity().getClass().getSimpleName());
                                    //currentField.clearField();
                                    nextField.setFieldEntity(new Grass());
                                }

                                System.out.println("After clearing field. Entity type: " + nextField.getEntity().getClass().getSimpleName());

                                if (isVisible) {
                                    // Remove bullet from field
                                    currentField.clearField();
                                }

                                System.out.println("After removing bullet. Entity type: " + nextField.getEntity().getClass().getSimpleName());



                                trackActiveBullets[bullet.getBulletId()] = 0;
                                tank.setNumberOfBullets(tank.getNumberOfBullets() - 1);
                                cancel();

                            } else {

                                if (isVisible) {
                                    // Remove bullet from field
                                    currentField.clearField();
                                }

                                nextField.setFieldEntity(bullet);
                                bullet.setParent(nextField);
                            }
                        }
                    }
                }, 0, BULLET_PERIOD);

                return true;
            } else if (builder.getIsActive() == 1) { // Builder fire
                BuilderAction b = new BuilderAction(monitor, game);
                return b.fire(tankId, bulletType);
            } else {
                SoldierAction s = new SoldierAction(monitor, game);
                return s.fire(tankId, bulletType);
            }
        }
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

    public int getSoldierHealth(long soldierId) {
        Soldier soldier = game.getSoldier((int)soldierId);
        if(soldier != null) {
            return soldier.getLife();
        }
        System.out.println("soldier is null");
        return 0;
    }

}
