package edu.unh.cs.cs619.bulletzone.repository;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Timer;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryGameRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    InMemoryGameRepository repo;
    Tank tank;
    @Before
    public void setUp() throws Exception {
        repo = new InMemoryGameRepository();
        tank = repo.join("");
    }

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