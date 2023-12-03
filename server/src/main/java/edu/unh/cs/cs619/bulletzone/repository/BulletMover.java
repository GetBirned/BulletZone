package edu.unh.cs.cs619.bulletzone.repository;

import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Grass;
import edu.unh.cs.cs619.bulletzone.model.HealthKit;
import edu.unh.cs.cs619.bulletzone.model.Hill;
import edu.unh.cs.cs619.bulletzone.model.Rocky;
import edu.unh.cs.cs619.bulletzone.model.Shield;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Thingamajig;
import edu.unh.cs.cs619.bulletzone.model.Vehicle;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.Water;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;

public class BulletMover {
    private final Timer timer = new Timer();
    private static final int BULLET_PERIOD = 200;

    public void fireHelper(Vehicle v, Bullet bullet, Object monitor, Game game, int[] trackActiveBullets) {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                synchronized (monitor) {
                    System.out.println("Active Bullet: " + v.getNumberOfBullets() + "---- Bullet ID: " + bullet.getIntValue());
                    FieldHolder currentField = bullet.getParent();
                    Direction direction = bullet.getDirection();
                    FieldHolder nextField = currentField
                            .getNeighbor(direction);

                    // Is the bullet visible on the field?
                    boolean isVisible = currentField.isPresent()
                            && (currentField.getEntity() == bullet);


                    if (nextField.isPresent()  && !(nextField.getEntity() instanceof Hill) && !(nextField.getEntity() instanceof Rocky)
                            && !(nextField.getEntity() instanceof Water)) {
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
                        v.setNumberOfBullets(v.getNumberOfBullets() - 1);
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
    }
}
