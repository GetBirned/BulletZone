package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Forest;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Hill;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Rocky;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Thingamajig;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;
import jdk.internal.org.jline.utils.Log;

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

            //System.out.println("Turning " + tankId + " " + direction.toString());

            // Find user
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                //Log.i(TAG, "Cannot find user with id: " + tankId);
                throw new TankDoesNotExistException(tankId);
            }

            long millis = System.currentTimeMillis();
            if (tank.getIsActive() == 1) {
                if(millis < tank.getLastMoveTime())
                    return false;

                tank.setLastMoveTime(millis+tank.getAllowedMoveInterval());

                /*try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }*/

                tank.setDirection(direction);

                return true;
            } else {
                Soldier soldier = game.getSoldiers().get(tankId);
                if(millis < soldier.getLastMoveTime())
                    return false;

                soldier.setLastMoveTime(millis+tank.getAllowedMoveInterval());

            /*try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }*/

                soldier.setDirection(direction);

                return true;
            }
        }
    }


    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
       // System.out.println("move before sync");
        synchronized (this.monitor) {
            // Find tank
          //  System.out.println("Moving " + tankId + " " + direction.toString());

            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                //Log.i(TAG, "Cannot find user with id: " + tankId);
                //return false;
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
                        || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp || nextField.getEntity() instanceof nukePowerUp) {
                    // If the next field is empty move the user



                    //Constraint to allow tanks on hills and rocky terrain and to slow them on hills
                    if (nextField.isPresent()) {
                        if (nextField.getEntity() instanceof Hill) {
                            if (!(parent.getEntity() instanceof Hill)) {
                                tank.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() * 1.5));
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
                    }
                    nextField.setFieldEntity(tank);
                    tank.setParent(nextField);

                    isCompleted = true;
                } else {
                    isCompleted = false;
                }

                return isCompleted;
            } else {
                Soldier soldier = game.getSoldiers().get(tankId);
                if (Direction.toByte(direction) != Direction.toByte(soldier.getDirection()) && Direction.toByte(direction) != Direction.opposite(soldier.getDirection())) {
                    return false;
                }


                long millis = System.currentTimeMillis();
                if(millis < soldier.getLastMoveTime())
                    return false;

                soldier.setLastMoveTime(millis + soldier.getAllowedMoveInterval());

                FieldHolder parent = soldier.getParent();

                FieldHolder nextField = parent.getNeighbor(direction);
                checkNotNull(parent.getNeighbor(direction), "Neightbor is not available");

                boolean isCompleted;
                if (!nextField.isPresent() || nextField.getEntity() instanceof Hill || nextField.getEntity() instanceof Rocky || nextField.getEntity() instanceof Forest
                 || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp || nextField.getEntity() instanceof nukePowerUp) {
                    // If the next field is empty move the user

                    //Constraint to allow soldiers on hills and rocky terrain and to slow them on rocky
                    if (nextField.isPresent()) {
                        if (nextField.getEntity() instanceof Rocky) {
                            if (!(parent.getEntity() instanceof Rocky)) {
                                soldier.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() * 1.5));
                            }
                        }
                    } else {
                        if (parent.getEntity() instanceof Rocky) {
                            soldier.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 1.5));
                        }
                    }


                    parent.clearField();

                    if(soldier.getPowerUpType() == 4) {
                        System.out.println("Restoring terrain. Current entity type: hill");
                        parent.setFieldEntity(new Hill());
                    } else if(soldier.getPowerUpType() == 5) {
                        System.out.println("Restoring terrain. Current entity type: rock");
                        parent.setFieldEntity(new Rocky());
                    } else if(soldier.getPowerUpType() == 6) {
                        System.out.println("Restoring terrain. Current entity type: forest");
                        parent.setFieldEntity(new Forest());
                    }


                    nextField.setFieldEntity(soldier);
                    soldier.setParent(nextField);

                    isCompleted = true;
                } else if (nextField.getEntity() instanceof Tank) {
                    Tank t = (Tank) nextField.getEntity();
                    if (t.getId() == soldier.getId())  { // make sure soldier is attempting to join it's own tank
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
                    return true;
                } else {
                    isCompleted = false;
                }

                return isCompleted;
            }
        }
    }


    public boolean fire(long tankId, int bulletType)
            throws TankDoesNotExistException, LimitExceededException {
        synchronized (this.monitor) {

            // Find tank
            Tank tank = game.getTanks().get(tankId);
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


                            if (nextField.isPresent()) {
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
                                } else if (nextField.getEntity() instanceof Wall) {
                                    Wall w = (Wall) nextField.getEntity();
                                    if (w.destructValue <= 0) {
                                        System.out.println("MAKING IT HERE ********************");
                                        game.getHolderGrid().get(w.getPos()).clearField();
                                    } else {
                                        System.out.println("Destruct Value before: " + w.destructValue);
                                        w.destructValue -= bullet.getDamage();
                                        System.out.println("Destruct Value after: " + w.destructValue);
                                    }
                                }
                                if (isVisible) {
                                    // Remove bullet from field
                                    currentField.clearField();
                                }
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
            } else { // Soldier fire
                Soldier soldier = game.getSoldiers().get(tankId);
                if (soldier.getNumberOfBullets() >= soldier.getAllowedNumberOfBullets())
                    return false;

                long millis = System.currentTimeMillis();
                if (millis < soldier.getLastFireTime()/*>tank.getAllowedFireInterval()*/) {
                    return false;
                }

                //Log.i(TAG, "Cannot find user with id: " + tankId);
                Direction direction = soldier.getDirection();
                FieldHolder parent = soldier.getParent();
                soldier.setNumberOfBullets(soldier.getNumberOfBullets() + 1);

                if (!(bulletType >= 1 && bulletType <= 3)) {
                    System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
                    bulletType = 1;
                }

                soldier.setLastFireTime(millis + bulletDelay[bulletType - 1]);

                int bulletId = 0;
                if (trackActiveBullets[0] == 0) {
                    bulletId = 0;
                    trackActiveBullets[0] = 1;
                } else if (trackActiveBullets[1] == 0) {
                    bulletId = 1;
                    trackActiveBullets[1] = 1;
                }

                // Create a new bullet to fire
                final Bullet bullet = new Bullet(tankId, direction, 5);
                // Set the same parent for the bullet.
                // This should be only a one way reference.
                bullet.setParent(parent);
                bullet.setBulletId(bulletId);

                // TODO make it nicer
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        synchronized (monitor) {
                            System.out.println("Soldier Active Bullet: " + soldier.getNumberOfBullets() + "---- Bullet ID: " + bullet.getIntValue());
                            FieldHolder currentField = bullet.getParent();
                            Direction direction = bullet.getDirection();
                            FieldHolder nextField = currentField
                                    .getNeighbor(direction);

                            // Is the bullet visible on the field?
                            boolean isVisible = currentField.isPresent()
                                    && (currentField.getEntity() == bullet);


                            if (nextField.isPresent()) {
                                // Something is there, hit it
                                nextField.getEntity().hit(bullet.getDamage());

                                if (nextField.getEntity() instanceof Tank) { // Tank Hit
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
                                } else if (nextField.getEntity() instanceof Wall) {
                                    Wall w = (Wall) nextField.getEntity();
                                    if (w.getIntValue() > 1000 && w.getIntValue() <= 2000) {
                                        game.getHolderGrid().get(w.getPos()).clearField();
                                    }
                                }
                                if (isVisible) {
                                    // Remove bullet from field
                                    currentField.clearField();
                                }
                                trackActiveBullets[bullet.getBulletId()] = 0;
                                soldier.setNumberOfBullets(soldier.getNumberOfBullets() - 1);
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
