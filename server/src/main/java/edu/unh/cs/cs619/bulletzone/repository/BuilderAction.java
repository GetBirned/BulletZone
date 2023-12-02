package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bridge;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
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
import edu.unh.cs.cs619.bulletzone.model.Thingamajig;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.Water;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;

public class BuilderAction {

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
    public BuilderAction(Object monitor, Game game) {
        this.monitor = monitor;
        this.game = game;
    }

    public boolean turn(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            checkNotNull(direction);

            // Find user
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                throw new TankDoesNotExistException(tankId);
            }


            long millis = System.currentTimeMillis();
            Builder builder = game.getBuilders().get(tankId);
            if(millis < builder.getLastMoveTime())
                return false;

            builder.setLastMoveTime(millis+tank.getAllowedMoveInterval());

            builder.setDirection(direction);
        }

        return true;
    }

    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                throw new TankDoesNotExistException(tankId);
            }

            Builder builder = game.getBuilders().get(tankId);
            if (Direction.toByte(direction) != Direction.toByte(builder.getDirection()) && Direction.toByte(direction) != Direction.opposite(builder.getDirection())) {
                return false;
            }


            long millis = System.currentTimeMillis();
            if(millis < builder.getLastMoveTime())
                return false;

            builder.setLastMoveTime(millis + builder.getAllowedMoveInterval());

            FieldHolder parent = builder.getParent();

            FieldHolder nextField = parent.getNeighbor(direction);
            checkNotNull(parent.getNeighbor(direction), "Neightbor is not available");
            boolean isCompleted;
            if (!nextField.isPresent() || nextField.getEntity() instanceof Hill || nextField.getEntity() instanceof Rocky || nextField.getEntity() instanceof Forest
                    || nextField.getEntity() instanceof Thingamajig || nextField.getEntity() instanceof applePowerUp || nextField.getEntity() instanceof nukePowerUp
                    || nextField.getEntity() instanceof Shield || nextField.getEntity() instanceof HealthKit
                    || nextField.getEntity() instanceof Grass || nextField.getEntity() instanceof Road ||
                    nextField.getEntity() instanceof Bridge ) {
                // If the next field is empty move the user

                //Constraint to allow soldiers on hills and rocky terrain and to slow them on rocky
                if (nextField.isPresent()) {
                    if (nextField.getEntity() instanceof Rocky) {
                        if (!(parent.getEntity() instanceof Rocky)) {
                            builder.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() * 1.5));
                        }
                    }
                } else {
                    if (parent.getEntity() instanceof Rocky) {
                        builder.setAllowedMoveInterval((int) (tank.getAllowedMoveInterval() / 1.5));
                    }
                }


                parent.clearField();

                if(builder.getPowerUpType() == 4) {
                    System.out.println("Restoring terrain. Current entity type: hill");
                    parent.setFieldEntity(new Hill());
                } else if(builder.getPowerUpType() == 5) {
                    System.out.println("Restoring terrain. Current entity type: rock");
                    parent.setFieldEntity(new Rocky());
                } else if(builder.getPowerUpType() == 6) {
                    System.out.println("Restoring terrain. Current entity type: forest");
                    parent.setFieldEntity(new Forest());
                } else if (builder.getPowerUpType() == 8) {
                    System.out.println("Restoring terrain. Current entity type: water");
                    parent.setFieldEntity(new Water());
                } else if (builder.getPowerUpType() == 11) {
                    System.out.println("Restoring terrain. Current entity type: bridge");
                    parent.setFieldEntity(new Bridge());
                } else if (builder.getPowerUpType() == 12) {
                    System.out.println("Restoring terrain. Current entity type: road");
                    parent.setFieldEntity(new Road());
                }


                nextField.setFieldEntity(builder);
                builder.setParent(nextField);

                isCompleted = true;
            } else if (nextField.getEntity() instanceof Tank) {
                Tank t = (Tank) nextField.getEntity();
                if (t.getId() == builder.getId())  { // make sure soldier is attempting to join it's own tank
                    if (t.getLife() != 0) { // cannot enter tank if its dead
                        if (builder.reenterTank(tank)) {
                            game.removeSoldier(tankId);
                            game.startEjectionCooldown();
                            builder.getParent().clearField();
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
                if (nextField.getEntity() instanceof Wall) {
                    ((Wall) nextField.getEntity()).takeDamagefromSoldier(builder.getLife());
                } else if (nextField.getEntity() instanceof Tank) {
                    ((Tank) nextField.getEntity()).takeDamagefromSoldier(builder.getLife());
                }
                isCompleted = false;
            }

        }
        return true;
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

            Builder builder = game.getBuilders().get(tankId);
            if (builder.getNumberOfBullets() >= builder.getAllowedNumberOfBullets())
                return false;

            long millis = System.currentTimeMillis();
            if (millis < builder.getLastFireTime()/*>tank.getAllowedFireInterval()*/) {
                return false;
            }

            //Log.i(TAG, "Cannot find user with id: " + tankId);
            Direction direction = builder.getDirection();
            FieldHolder parent = builder.getParent();
            builder.setNumberOfBullets(builder.getNumberOfBullets() + 1);

            if (!(bulletType >= 1 && bulletType <= 3)) {
                System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
                bulletType = 1;
            }

            builder.setLastFireTime(millis + bulletDelay[bulletType - 1]);

            int bulletId = 0;
            if (trackActiveBullets[0] == 0) {
                bulletId = 0;
                trackActiveBullets[0] = 1;
            } else if (trackActiveBullets[1] == 0) {
                bulletId = 1;
                trackActiveBullets[1] = 1;
            }

            // Create a new bullet to fire
            final Bullet bullet = new Bullet(builder.getId(), direction, 10);
            // Set the same parent for the bullet.
            // This should be only a one way reference.
            bullet.setParent(parent);
            bullet.setBulletId(bulletId);

            // TODO make it nicer
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    synchronized (monitor) {
                        System.out.println("Builder Active Bullet: " + builder.getNumberOfBullets() + "---- Bullet ID: " + bullet.getIntValue());
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
                            } else if (nextField.getEntity() instanceof Builder) { // Soldier Hit
                                Builder b = (Builder) nextField.getEntity();
                                System.out.println("soldier is hit, soldier life: " + b.getLife());
                                if (b.getLife() <= 0) {
                                    Tank t = game.getTank(b.getIp());
                                    t.getParent().clearField();
                                    t.setParent(null);
                                    b.getParent().clearField();
                                    b.setParent(null);
                                    game.removeSoldier(b.getId());
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
                            }

                            System.out.println("Before clearing field. Entity type: " + nextField.getEntity().getClass().getSimpleName());

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
                            trackActiveBullets[bullet.getBulletId()] = 0;
                            builder.setNumberOfBullets(builder.getNumberOfBullets() - 1);
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

