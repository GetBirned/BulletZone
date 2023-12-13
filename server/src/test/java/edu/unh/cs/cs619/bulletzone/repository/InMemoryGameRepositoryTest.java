package edu.unh.cs.cs619.bulletzone.repository;

import org.apache.tomcat.jni.Time;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

import javax.management.timer.Timer;

import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.HealthKit;
import edu.unh.cs.cs619.bulletzone.model.Shield;
import edu.unh.cs.cs619.bulletzone.model.nukePowerUp;
import edu.unh.cs.cs619.bulletzone.model.Bridge;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.GameBoard;
import edu.unh.cs.cs619.bulletzone.model.GameBoardBuilder;
import edu.unh.cs.cs619.bulletzone.model.Grass;
import edu.unh.cs.cs619.bulletzone.model.GridEvent;
import edu.unh.cs.cs619.bulletzone.model.HijackTrap;
import edu.unh.cs.cs619.bulletzone.model.Hill;
import edu.unh.cs.cs619.bulletzone.model.Meadow;
import edu.unh.cs.cs619.bulletzone.model.Mine;
import edu.unh.cs.cs619.bulletzone.model.Rocky;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.TankLocation;
import edu.unh.cs.cs619.bulletzone.model.Water;
import edu.unh.cs.cs619.bulletzone.model.applePowerUp;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryGameRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    InMemoryGameRepository repo;
    Tank tank;
    GameBoardBuilder gbb;
    GameBoard gb;
    @Before
    public void setUp() throws Exception {
        tank = repo.join("");
        gbb = new GameBoardBuilder();
        gb = gbb.build(new Grass());

    }

    //Milestone 1 Tests
    @Test
    public void testJoin() throws Exception {
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertSame(tank.getDirection(), Direction.Up);
        Assert.assertNotNull(tank.getParent());
    }

    @Test
    public void testTurn() throws Exception {
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertSame(tank.getDirection(), Direction.Up);
        Assert.assertNotNull(tank.getParent());

        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        Assert.assertSame(tank.getDirection(), Direction.Right);

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Right);
    }

    @Test
    public void testMoveRL() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Up));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Down));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Right));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Right));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Left));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Left));
    }

    @Test
    public void testMoveUD() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Down));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Right));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Left));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Up));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Down));

    }

    @Test
    public void testTimedMove() throws Exception {
        if (!tank.getParent().getNeighbor(Direction.Down).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Down));
        }
        Assert.assertFalse(repo.move(tank.getId(), Direction.Up));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        if (!tank.getParent().getNeighbor(Direction.Up).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Up));
        }

    }
    @Test
    public void testTimedTurn() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Left));
        byte i = 0;
        while(System.currentTimeMillis() < tank.getLastMoveTime() - 50) {
            Assert.assertFalse(repo.turn(tank.getId(), Direction.fromByte(i)));
            i+=2;
            if (i > 6) {
                i = 0;
            }
        }

    }
    @Test
    public void testTimedFire() throws Exception {
        Assert.assertTrue(repo.fire(tank.getId(), 1));
        while(System.currentTimeMillis() < tank.getLastFireTime() - 50) {
            Assert.assertFalse(repo.fire(tank.getId(), 1));
        }

    }

    @Test
    public void historyTest() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Up));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Up));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Left));
        Stack<GridEvent> s = repo.getCommandHistory();
        Assert.assertNotEquals(tank.getId() + " - Move", s.peek().getCommand());
        Assert.assertEquals(tank.getId() + " - Turn", s.pop().getCommand());
        Assert.assertEquals(tank.getId() + " - Turn", s.pop().getCommand());
        Assert.assertEquals(tank.getId() + " - Move", s.pop().getCommand());
        Assert.assertEquals(tank.getId() + " - Turn", s.pop().getCommand());
    }
    @Test
    public void timedHistoryTest() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Up));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Down));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Timestamp t = new Timestamp(System.currentTimeMillis());
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Left));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Right));
        while(System.currentTimeMillis() < tank.getLastMoveTime()); // waits 500 ms
        Assert.assertTrue(repo.move(tank.getId(), Direction.Left));

        LinkedList<GridEvent> s = repo.getHistory(t);

        Assert.assertEquals(s.size(), 4);

        Assert.assertEquals(tank.getId() + " - Turn", s.get(0).getCommand());
        Assert.assertEquals(tank.getId() + " - Turn", s.get(1).getCommand());
        Assert.assertEquals(tank.getId() + " - Move", s.get(2).getCommand());
        Assert.assertEquals(tank.getId() + " - Move", s.get(3).getCommand());
    }

    //Milestone 2 Tests
//---------------------------------------------------------------------------------------

    @Test
    public void testSinglePowerupBehaviors() throws Exception {
        //fusion p-up test
        Tank t2 = repo.join("tank2");
        t2.getParent().clearField();
        Assert.assertEquals(500, t2.getAllowedMoveInterval());
        Assert.assertEquals(1500, t2.getAllowedFireInterval());
        Assert.assertEquals(2, t2.getAllowedNumberOfBullets());
        repo.setTankPowerup(t2.getId(), 2);
        Assert.assertEquals((int) (500 * 1.25), t2.getAllowedMoveInterval());
        Assert.assertEquals(2 * 2, t2.getAllowedNumberOfBullets());
        Assert.assertEquals((int) (500 * 1.5), t2.getAllowedFireInterval());
        repo.leave(t2.getId());

        //anti grav p-up test
        Tank t3 = repo.join("tank3");
        t3.getParent().clearField();
        Assert.assertEquals(500, t3.getAllowedMoveInterval());
        Assert.assertEquals(1500, t3.getAllowedFireInterval());
        Assert.assertEquals(2, t3.getAllowedNumberOfBullets());
        repo.setTankPowerup(t3.getId(), 3);
        Assert.assertEquals((500 / 2), t3.getAllowedMoveInterval());
        Assert.assertEquals((500 + 100), t3.getAllowedFireInterval());
        repo.leave(t3.getId());
    }

    @Test
    public void testMultiplePowerupBehaviors() throws Exception{
        //fusion then anti grav
        Tank t4 = repo.join("tank4");
        t4.getParent().clearField();
        Assert.assertEquals(500, t4.getAllowedMoveInterval());
        Assert.assertEquals(1500, t4.getAllowedFireInterval());
        Assert.assertEquals(2, t4.getAllowedNumberOfBullets());
        repo.setTankPowerup(t4.getId(), 2);
        Assert.assertEquals((int) (500 * 1.25), t4.getAllowedMoveInterval());
        Assert.assertEquals(2 * 2, t4.getAllowedNumberOfBullets());
        Assert.assertEquals((int) (500 / 2), t4.getAllowedFireInterval());
        long t4currMoveInterval = t4.getAllowedMoveInterval();
        long t4currFireInterval = t4.getAllowedFireInterval();
        repo.setTankPowerup(t4.getId(), 3);
        Assert.assertEquals((t4currMoveInterval / 2), t4.getAllowedMoveInterval());
        Assert.assertEquals((t4currFireInterval + 100), t4.getAllowedFireInterval());
        repo.leave(t4.getId());

        //anti grav then fusion
        Tank t5 = repo.join("tank5");
        t5.getParent().clearField();
        Assert.assertEquals(500, t5.getAllowedMoveInterval());
        Assert.assertEquals(1500, t5.getAllowedFireInterval());
        Assert.assertEquals(2, t5.getAllowedNumberOfBullets());
        repo.setTankPowerup(t5.getId(), 2);
        Assert.assertEquals((int) (500 * 1.25), t5.getAllowedMoveInterval());
        Assert.assertEquals(2 * 2, t5.getAllowedNumberOfBullets());
        Assert.assertEquals((int) (500 / 2), t5.getAllowedFireInterval());
        long t5currMoveInterval = t5.getAllowedMoveInterval();
        long t5currFireInterval = t5.getAllowedFireInterval();
        repo.setTankPowerup(t5.getId(), 3);
        Assert.assertEquals((t5currMoveInterval / 2), t5.getAllowedMoveInterval());
        Assert.assertEquals((t5currFireInterval + 100), t5.getAllowedFireInterval());
        repo.leave(t5.getId());
    }
    @Test
    public void testSinglePowerupTerrainMovement() throws Exception {
        Tank tank2 = repo.join("tanktwo");
        TankLocation t = repo.getGame().findTank(tank2, tank2.getId());
        Assert.assertNotNull(t);
        tank2.getParent().clearField();
        Assert.assertEquals(500, tank2.getAllowedMoveInterval());
        Assert.assertEquals(1500, tank2.getAllowedFireInterval());
        Assert.assertEquals(2, tank2.getAllowedNumberOfBullets());


        repo.getGame().getGameBoard().setEntity(t.getColumn() + 1, t.getRow(), new Hill());
        tank2.setDirection(Direction.Right);
        Assert.assertTrue(repo.move(tank2.getId(), Direction.Right));
        Assert.assertEquals(tank.getAllowedMoveInterval(), 750);

        while(tank2.getLastMoveTime() > System.currentTimeMillis());

        repo.getGame().getGameBoard().setEntity(t.getColumn() - 1, t.getRow(), new Rocky());
        Assert.assertTrue(repo.move(tank2.getId(), Direction.Left));
        Assert.assertEquals(tank2.getAllowedMoveInterval(), 500);

        long soldierID = repo.deploySoldier(tank2.getId()).getResult();
        Soldier soldier = repo.getGame().getSoldier(soldierID);
        TankLocation s = repo.getGame().findSoldier(soldier, soldierID);

        Assert.assertNotNull(s);

        repo.getGame().getGameBoard().setEntity(s.getColumn() + 1, s.getRow(), new Rocky());
        Assert.assertTrue(repo.move(soldierID, Direction.Right));
        Assert.assertEquals(soldier.getAllowedMoveInterval(), 750);
        repo.getGame().getGameBoard().setEntity(s.getColumn() - 1, s.getRow(), new Hill());
        Assert.assertTrue(repo.move(soldierID, Direction.Left));
        Assert.assertEquals(soldier.getAllowedMoveInterval(), 500);
    }
    @Test
    public void testRockyHillyTimedMovement() throws Exception {
        TankLocation t = repo.getGame().findTank(tank, tank.getId());
        Assert.assertNotNull(t);

        repo.getGame().getGameBoard().setEntity(t.getColumn() + 1, t.getRow(), new Hill());
        tank.setDirection(Direction.Right);
        Assert.assertTrue(repo.move(tank.getId(), Direction.Right));
        Assert.assertEquals(tank.getAllowedMoveInterval(), 750);

        while(tank.getLastMoveTime() > System.currentTimeMillis());

        repo.getGame().getGameBoard().setEntity(t.getColumn() - 1, t.getRow(), new Rocky());
        Assert.assertTrue(repo.move(tank.getId(), Direction.Left));
        Assert.assertEquals(tank.getAllowedMoveInterval(), 500);

        long soldierID = repo.deploySoldier(tank.getId()).getResult();
        Soldier soldier = repo.getGame().getSoldier(soldierID);
        TankLocation s = repo.getGame().findSoldier(soldier, soldierID);

        if(s == null) {
            return;
        }

        repo.getGame().getGameBoard().setEntity(s.getColumn() + 1, s.getRow(), new Rocky());
        Assert.assertTrue(repo.move(soldierID, Direction.Right));
        Assert.assertEquals(soldier.getAllowedMoveInterval(), 750);
        repo.getGame().getGameBoard().setEntity(s.getColumn() - 1, s.getRow(), new Hill());
        Assert.assertTrue(repo.move(soldierID, Direction.Left));
        Assert.assertEquals(soldier.getAllowedMoveInterval(), 500);
    }

    //Milestone 3 Tests
//---------------------------------------------------------------------------------------
//    Tests for behavior of clients joining/disconnecting


    @Test
    public void testJoinDisconnectM3() throws Exception {
        Tank t11 = repo.join("t11");
        Assert.assertTrue(t11.getId() >= 0);
        Assert.assertNotNull(t11.getDirection());
        Assert.assertSame(t11.getDirection(), Direction.Up);
        Assert.assertNotNull(t11.getParent());

        long id = t11.getId();
        Assert.assertTrue(repo.turn(id, Direction.Left));
        repo.leave(t11.getId());
        thrown.expect(TankDoesNotExistException.class);
        repo.turn(id, Direction.Right);
        thrown.expect(TankDoesNotExistException.class);
        repo.move(id, Direction.Right);
        thrown.expect(TankDoesNotExistException.class);
        repo.fire(id, 1);

    }
//    and times/non timed vehicle interactions (turn, move, fire) on milestone 3 terrain
    @Test
    public void testVehicleInteractionsOnNewTerrainM3() throws Exception {
        Direction d;
        int x;
        Tank t12 = repo.join("t12");
        TankLocation t = repo.getGame().findTank(t12, t12.getId());
        Assert.assertNotNull(t);
        //setting direct for tank/soldier/builder to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Water());
        t12.setDirection(d);

        //tanks cannot go on water
        Assert.assertFalse(repo.move(t12.getId(), d));
        Assert.assertTrue(repo.fire(t12.getId(), 1));
        while(t12.getLastMoveTime() > System.currentTimeMillis());
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Bridge());
        t12.setDirection(d);

        //tanks can go on bridges
        Assert.assertTrue(repo.move(t12.getId(), d));
        while(t12.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.fire(t12.getId(), 1));
        while(t12.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.turn(t12.getId(), Direction.Down));



        long soldierID = repo.deploySoldier(t12.getId()).getResult();
        Soldier soldier = repo.getGame().getSoldier(soldierID);
        TankLocation s = repo.getGame().findSoldier(soldier, soldierID);
        Assert.assertNotNull(s);

        if (s.getColumn() > 0) {
            d = Direction.Left;
            x = s.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = s.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, s.getRow(), new Water());
        soldier.setDirection(d);
        //soldiers cannot go on water
        Assert.assertFalse(repo.move(soldier.getId(), d));
        Assert.assertTrue(repo.fire(soldier.getId(), 1));
        while(soldier.getLastMoveTime() > System.currentTimeMillis());

        repo.getGame().getGameBoard().setEntity(x, s.getRow(), new Bridge());
        soldier.setDirection(d);
        Assert.assertTrue(repo.move(soldier.getId(), d));
        while(soldier.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.fire(soldier.getId(), 1));
        while(soldier.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.turn(soldier.getId(), Direction.Down));

        repo.controlTank(t12.getId());

        long builderID = repo.controlBuilder(t12.getId()).getResult();
        Builder builder = repo.getGame().getBuilder(builderID);
        TankLocation b = repo.getGame().findBuilder(builder, soldierID);
        Assert.assertNotNull(b);

        if (b.getColumn() > 0) {
            d = Direction.Left;
            x = b.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = b.getColumn() + 1;
        }

        //builders can go on water
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Water());
        soldier.setDirection(d);
        Assert.assertTrue(repo.move(builder.getId(), d));
        Assert.assertTrue(repo.fire(builder.getId(), 1));
        while(builder.getLastMoveTime() > System.currentTimeMillis());

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Bridge());
        soldier.setDirection(d);
        Assert.assertTrue(repo.move(builder.getId(), d));
        while(soldier.getLastFireTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.fire(builder.getId(), 1));
        while(soldier.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.turn(builder.getId(), Direction.Down));

        repo.leave(t12.getId());

    }
//    Tests for power-up-involved vehicle interactions (turning, moving, building, and firing based on power-ups present) on open meadow
    @Test
    public void testPowerUpsOnMeadowsM3() throws Exception {
        Direction d;
        int x;
        gbb.build(new Grass());
        Tank t13 = repo.join("t13");

        TankLocation t = repo.getGame().findTank(t13, t13.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new applePowerUp());
        t13.setDirection(d);
        Assert.assertTrue(t13.getAllowedMoveInterval() == 500);
        Assert.assertTrue(t13.getAllowedFireInterval() == 1500);
        Assert.assertTrue(repo.move(t13.getId(), d));
        Assert.assertTrue(t13.getAllowedMoveInterval() == 250);
        Assert.assertTrue(t13.getAllowedFireInterval() == 1650);

        Assert.assertTrue(repo.fire(t13.getId(), 1));


        repo.leave(t13.getId());


        gbb.build(new Grass());
        Tank t132 = repo.join("t132");

        t = repo.getGame().findTank(t132, t132.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new nukePowerUp());
        t132.setDirection(d);
        Assert.assertTrue(t132.getAllowedMoveInterval() == 500);
        Assert.assertTrue(t132.getAllowedFireInterval() == 1500);
        Assert.assertTrue(repo.move(t132.getId(), d));
        Assert.assertTrue(t132.getAllowedMoveInterval() == 625);
        Assert.assertTrue(t132.getAllowedFireInterval() == 750);

        Assert.assertTrue(repo.fire(t132.getId(), 1));


        repo.leave(t132.getId());
    }
//    Tests for powerup involved damage reductions and effect cancellation (involves time)
    @Test
    public void testNewPowerupsM3() throws Exception {
        Direction d;
        int x;
        gbb.build(new Grass());
        Tank t14 = repo.join("t14");
        TankLocation t = repo.getGame().findTank(t14, t14.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        //testing shield p-up
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Shield());
        t14.setDirection(d);
        Assert.assertTrue(repo.move(t14.getId(), d));
        Assert.assertTrue(t14.getLife() == 100);
        t14.hit(10);
        Assert.assertTrue(t14.getLife() >= 90 && t14.getLife() < 100);
        //shield should regen after taking damage up to 100
        Time.sleep(10000);
        Assert.assertTrue(t14.getLife() > 90 && t14.getLife() <= 100);
        repo.leave(t14.getId());

        Tank t142 = repo.join("t142");
        t = repo.getGame().findTank(t142, t142.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        //testing health kit p-up
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new HealthKit());
        t142.setDirection(d);
        Assert.assertTrue(repo.move(t142.getId(), d));
        Assert.assertTrue(t142.getLife() == 100);
        t142.hit(10);
        Assert.assertTrue(t142.getLife() >= 90 && t142.getLife() < 100);
        //shield should regen after taking damage up to 100
        Time.sleep(10000);
        Assert.assertTrue(t14.getLife() > 90 && t14.getLife() <= 100);
        repo.leave(t142.getId());

    }
//    Tests for powerup involved vehicle interactions on other terrains
    @Test
    public void testPowerUpsOnOtherTerrainsM3() throws Exception {
        Direction d;
        int x;
        gbb.build(new Hill());
        Tank t15 = repo.join("t15");

        TankLocation t = repo.getGame().findTank(t15, t15.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new applePowerUp());
        t15.setDirection(d);
        Assert.assertTrue(t15.getAllowedMoveInterval() == 750);
        Assert.assertTrue(t15.getAllowedFireInterval() == 1500);
        Assert.assertTrue(repo.move(t15.getId(), d));
        Assert.assertTrue(t15.getAllowedMoveInterval() == 375);
        Assert.assertTrue(t15.getAllowedFireInterval() == 1650);

        Assert.assertTrue(repo.fire(t15.getId(), 1));


        repo.leave(t15.getId());

        Tank t152 = repo.join("t152");

        t = repo.getGame().findTank(t152, t152.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new nukePowerUp());
        t152.setDirection(d);
        Assert.assertTrue(t152.getAllowedMoveInterval() == 750);
        Assert.assertTrue(t152.getAllowedFireInterval() == 1500);
        Assert.assertTrue(repo.move(t152.getId(), d));
        Assert.assertTrue(t152.getAllowedMoveInterval() == 825);
        Assert.assertTrue(t152.getAllowedFireInterval() == 750);

        Assert.assertTrue(repo.fire(t152.getId(), 1));


        repo.leave(t152.getId());
    }
//    Tests for powerup involved vehicle interactions on roads and decking
    @Test
    public void testPowerupsOnRoadsAndDeckingM3() throws Exception {
        Direction d;
        int x;
        gbb.build(new Hill());
        Tank t16 = repo.join("t16");

        TankLocation t = repo.getGame().findTank(t16, t16.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        //tanks cannot go on water
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Water());
        t16.setDirection(d);
        Assert.assertFalse(repo.move(t16.getId(), d));
        while(t16.getLastMoveTime() > System.currentTimeMillis());

        //tanks can go on bridges
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Bridge());
        Assert.assertTrue(repo.move(t16.getId(), d));
        while(t16.getLastMoveTime() > System.currentTimeMillis());

        Assert.assertTrue(repo.move(t16.getId(), Direction.opposite(d)));
        while(t16.getLastMoveTime() > System.currentTimeMillis());

        //tanks go faster on roads
        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Bridge());
        Assert.assertTrue(repo.move(t16.getId(), d));

        Assert.assertTrue(t16.getAllowedMoveInterval() == 250);


        repo.leave(t16.getId());
    }
//    Tests for builders building/dismantling and attempts to interrupt with other movement/firing during that time
    @Test
    public void testBuildersBehaviorM3() throws Exception {
        Direction d;
        int x;
        Tank t17 = repo.join("t17");
        repo.controlBuilder(t17.getId());
        Builder b17 = repo.getGame().getBuilder(t17.getId());

        TankLocation t = repo.getGame().findBuilder(b17, b17.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }
        b17.setDirection(d);
        //build a wall
        Assert.assertTrue(repo.buildImprovement(1, b17.getId()).getResult() == 1);

        Assert.assertFalse(repo.move(b17.getId(), Direction.opposite(d)));

        Assert.assertTrue(repo.dismantleImprovement(b17.getId()).getResult() == 1);

        //build a wall
        Assert.assertTrue(repo.buildImprovement(1, b17.getId()).getResult() == 1);
        //try to interrupt build
        b17.hit(1);
        Time.sleep(10000);
        Assert.assertTrue(repo.dismantleImprovement(b17.getId()).getResult() == 1);
        //try to interrupt dismantle
        b17.hit(1);

        repo.leave(t17.getId());
    }
//    Tests for picking up and ejecting powerups,
    @Test
    public void testPowerupsPickingUpAndEjectionM3() throws Exception {
        Direction d;
        int x;
        Tank t18 = repo.join("t18");
        TankLocation t = repo.getGame().findTank(t18, t18.getId());
        Assert.assertNotNull(t);

        //setting tank direction to be able to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new applePowerUp());
        t18.setDirection(d);
        Assert.assertTrue(repo.move(t18.getId(), d));
        Assert.assertTrue(t18.getPowerUpType() == new applePowerUp().getIntValue());

        //eject tank powerup
        Assert.assertTrue(repo.getTankPowerup(t18.getId()) != -1);
        Assert.assertTrue(t18.getPowerUpType() != new applePowerUp().getIntValue());


        repo.leave(t18.getId());
    }

//    Tests for activation and destruction of mines and/or hijack traps when various units enter squares that contain them
    @Test
    public void testTrapActivationAndDestructionM3() throws Exception {
        Direction d;
        int x;
        Tank t20 = repo.join("t20");
        TankLocation t = repo.getGame().findTank(t20, t20.getId());
        Assert.assertNotNull(t);
        //setting direct for tank/soldier/builder to move
        if (t.getColumn() > 0) {
            d = Direction.Left;
            x = t.getColumn() - 1;
        } else {
            d = Direction.Right;
            x = t.getColumn() + 1;
        }

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new Mine());
        t20.setDirection(d);
        Assert.assertTrue(repo.move(t20.getId(), d));
        Assert.assertTrue(t20.getLife() == 90);
        while(t20.getLastMoveTime() < System.currentTimeMillis());
        Assert.assertTrue(repo.move(t20.getId(), Direction.opposite(d)));
        //mine square returns to grass after the mine is activated
        Assert.assertTrue(Objects.equals(tank.getParent().getNeighbor(d).getEntity(), new Grass()));

        repo.getGame().getGameBoard().setEntity(x, t.getRow(), new HijackTrap());
        while(t20.getLastMoveTime() < System.currentTimeMillis());
        Assert.assertTrue(repo.move(t20.getId(), d));

        Assert.assertTrue(t20.getPowerUpType() == new HijackTrap().getIntValue());
        //tank being hit removes hijack trap
        t20.hit(1);
        Assert.assertTrue(t20.getPowerUpType() == -1);
        while(t20.getLastMoveTime() < System.currentTimeMillis());
        Assert.assertTrue(repo.move(t20.getId(), Direction.opposite(d)));
        //hijack square returns to grass after the mine is activated
        Assert.assertTrue(Objects.equals(tank.getParent().getNeighbor(d).getEntity(), new Grass()));

        while(t20.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.move(t20.getId(), Direction.opposite(d)));
        long soldierID = repo.deploySoldier(t20.getId()).getResult();
        Soldier s = repo.getGame().getSoldiers().get(soldierID);
        s.setDirection(d);
        repo.buildTrap(1, soldierID, 1);
        while(t20.getLastMoveTime() > System.currentTimeMillis());
        Assert.assertTrue(repo.move(soldierID, d));
        Assert.assertTrue(s.getLife() == 40);

        repo.leave(t20.getId());
    }

    @Test
    public void testDisconnect() throws Exception {
        long id = tank.getId();
        Assert.assertTrue(repo.turn(id, Direction.Left));
        repo.leave(tank.getId());
        thrown.expect(TankDoesNotExistException.class);
        repo.turn(id, Direction.Right);
        thrown.expect(TankDoesNotExistException.class);
        repo.move(id, Direction.Right);
        thrown.expect(TankDoesNotExistException.class);
        repo.fire(id, 1);
    }
}