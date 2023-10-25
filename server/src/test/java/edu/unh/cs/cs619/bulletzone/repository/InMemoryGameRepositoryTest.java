package edu.unh.cs.cs619.bulletzone.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

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
        tank = repo.join("");
    }

    @Test
    public void testJoin() throws Exception {
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertTrue(tank.getDirection() == Direction.Up);
        Assert.assertNotNull(tank.getParent());
    }

    @Test
    public void testTurn() throws Exception {
        Tank tank = repo.join("");
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertTrue(tank.getDirection() == Direction.Up);
        Assert.assertNotNull(tank.getParent());

        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        Assert.assertTrue(tank.getDirection() == Direction.Right);

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Right);
    }

    @Test
    public void testMoveRL() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Up));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Down));
        if (tank.getParent().getNeighbor(Direction.Right).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Right));
            Assert.assertTrue(repo.move(tank.getId(), Direction.Left));
        } else {
            Assert.assertFalse(repo.move(tank.getId(), Direction.Right));
        }

        if (tank.getParent().getNeighbor(Direction.Left).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Left));
            Assert.assertTrue(repo.move(tank.getId(), Direction.Right));
        } else {
            Assert.assertFalse(repo.move(tank.getId(), Direction.Left));
        }
    }

    @Test
    public void testMoveUD() throws Exception {
        Assert.assertTrue(repo.turn(tank.getId(), Direction.Up));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Right));
        Assert.assertFalse(repo.move(tank.getId(), Direction.Left));
        if (tank.getParent().getNeighbor(Direction.Up).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Up));
            Assert.assertTrue(repo.move(tank.getId(), Direction.Down));
        } else {
            Assert.assertFalse(repo.move(tank.getId(), Direction.Up));
        }

        if (tank.getParent().getNeighbor(Direction.Down).isPresent()) {
            Assert.assertTrue(repo.move(tank.getId(), Direction.Down));
            Assert.assertTrue(repo.move(tank.getId(), Direction.Up));
        } else {
            Assert.assertFalse(repo.move(tank.getId(), Direction.Down));
        }
    }
    @Test
    public void testFire() throws Exception {

    }

    @Test
    public void testLeave() throws Exception {

    }
}