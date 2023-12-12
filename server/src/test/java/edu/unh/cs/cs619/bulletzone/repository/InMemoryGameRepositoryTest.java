package edu.unh.cs.cs619.bulletzone.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Stack;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.GameBoard;
import edu.unh.cs.cs619.bulletzone.model.GameBoardBuilder;
import edu.unh.cs.cs619.bulletzone.model.GridEvent;
import edu.unh.cs.cs619.bulletzone.model.Hill;
import edu.unh.cs.cs619.bulletzone.model.Rocky;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.TankLocation;

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
        Tank tank = repo.join("");
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

        //anti grav p-up test
        Tank t3 = repo.join("tank3");
        t3.getParent().clearField();
        Assert.assertEquals(500, t3.getAllowedMoveInterval());
        Assert.assertEquals(1500, t3.getAllowedFireInterval());
        Assert.assertEquals(2, t3.getAllowedNumberOfBullets());
        repo.setTankPowerup(t3.getId(), 3);
        Assert.assertEquals((500 / 2), t3.getAllowedMoveInterval());
        Assert.assertEquals((500 + 100), t3.getAllowedFireInterval());

    }

    @Test
    public void testMultiplePowerupBehaviors() {
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
    }
    @Test
    public void testSinglePowerupTerrainMovement() throws Exception {
        TankLocation t = repo.getGame().findTank(tank, tank.getId());
        if(t == null) {
            return;
        }
        tank.getParent().clearField();
        Assert.assertEquals(500, tank.getAllowedMoveInterval());
        Assert.assertEquals(1500, tank.getAllowedFireInterval());
        Assert.assertEquals(2, tank.getAllowedNumberOfBullets());


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
    @Test
    public void testRockyHillyTimedMovement() throws Exception {
        TankLocation t = repo.getGame().findTank(tank, tank.getId());
        if(t == null) {
            return;
        }

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