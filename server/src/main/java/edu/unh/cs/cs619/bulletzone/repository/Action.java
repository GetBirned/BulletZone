package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.SQLOutput;
import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Wall;

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

            System.out.println("Turning " + tankId + " in " + direction.toString());

            // Find user
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                //Log.i(TAG, "Cannot find user with id: " + tankId);
                throw new TankDoesNotExistException(tankId);
            }

            long millis = System.currentTimeMillis();
            if(millis < tank.getLastMoveTime())
                return false;

            tank.setLastMoveTime(millis+tank.getAllowedMoveInterval());

            /*try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }*/

            tank.setDirection(direction);

            return true; // TODO check
        }
    }


    public boolean move(long tankId, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        System.out.println("move before sync");
        synchronized (this.monitor) {
            // Find tank
            System.out.println("Moving " + tankId + " in " + direction.toString());

            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                //Log.i(TAG, "Cannot find user with id: " + tankId);
                //return false;
                throw new TankDoesNotExistException(tankId);
            }

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
            checkNotNull(parent.getNeighbor(direction), "Neightbor is not available");

            boolean isCompleted;
            if (!nextField.isPresent()) {
                // If the next field is empty move the user

                /*try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }*/

                parent.clearField();
                nextField.setFieldEntity(tank);
                tank.setParent(nextField);

                isCompleted = true;
            } else {
                isCompleted = false;
            }

            return isCompleted;
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

            if(tank.getNumberOfBullets() >= tank.getAllowedNumberOfBullets())
                return false;

            long millis = System.currentTimeMillis();
            if(millis < tank.getLastFireTime()/*>tank.getAllowedFireInterval()*/){
                return false;
            }

            //Log.i(TAG, "Cannot find user with id: " + tankId);
            Direction direction = tank.getDirection();
            FieldHolder parent = tank.getParent();
            tank.setNumberOfBullets(tank.getNumberOfBullets() + 1);

            if(!(bulletType>=1 && bulletType<=3)) {
                System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
                bulletType = 1;
            }

            tank.setLastFireTime(millis + bulletDelay[bulletType - 1]);

            int bulletId=0;
            if(trackActiveBullets[0]==0){
                bulletId = 0;
                trackActiveBullets[0] = 1;
            }else if(trackActiveBullets[1]==0){
                bulletId = 1;
                trackActiveBullets[1] = 1;
            }

            // Create a new bullet to fire
            final Bullet bullet = new Bullet(tankId, direction, bulletDamage[bulletType-1]);
            // Set the same parent for the bullet.
            // This should be only a one way reference.
            bullet.setParent(parent);
            bullet.setBulletId(bulletId);

            // TODO make it nicer
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    synchronized (monitor) {
                        System.out.println("Active Bullet: "+tank.getNumberOfBullets()+"---- Bullet ID: "+bullet.getIntValue());
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

                            if ( nextField.getEntity() instanceof  Tank){
                                Tank t = (Tank) nextField.getEntity();
                                System.out.println("tank is hit, tank life: " + t.getLife());
                                if (t.getLife() <= 0 ){
                                    t.getParent().clearField();
                                    t.setParent(null);
                                    game.removeTank(t.getId());
                                }
                            }
                            else if ( nextField.getEntity() instanceof Wall){
                                Wall w = (Wall) nextField.getEntity();
                                if (w.getIntValue() >1000 && w.getIntValue()<=2000 ){
                                    game.getHolderGrid().get(w.getPos()).clearField();
                                }
                            }
                            if (isVisible) {
                                // Remove bullet from field
                                currentField.clearField();
                            }
                            trackActiveBullets[bullet.getBulletId()]=0;
                            tank.setNumberOfBullets(tank.getNumberOfBullets()-1);
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
