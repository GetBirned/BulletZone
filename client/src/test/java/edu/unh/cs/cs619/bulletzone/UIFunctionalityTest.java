package edu.unh.cs.cs619.bulletzone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;


public class UIFunctionalityTest {
    @InjectMocks
    BulletZoneRestClient restClient = Mockito.mock(BulletZoneRestClient.class);

    @Mock
    private LongWrapper longWrapper;

    @Mock
    private GridWrapper gridWrapper;

    @Mock
    private BooleanWrapper booleanWrapper;

    int tankId = 1;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @After
    public void release() {
        reset(restClient);
    }

    @Test
    public void testJoin() {
        when(restClient.join()).thenReturn(longWrapper);
        assertNotNull(restClient.join());
    }

    @Test
    public void validTurn_0() throws AssertionError {
        byte dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void validTurn_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void validTurn_3() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void validTurn_4() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void validTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
        dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void validTurnSequence_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void invalidTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertFalse(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void invalidTurnSequence_1() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertFalse(restClient.turn(tankId, dir).isResult());
    }

    @Test
    public void checkFireTime_0() throws InterruptedException {
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assertTrue(restClient.fire(tankId).isResult());
        Thread.sleep(1000);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assertFalse(restClient.fire(tankId).isResult());
    }

    @Test
    public void checkFireTime_1() throws InterruptedException {
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assertTrue(restClient.fire(tankId).isResult());
        Thread.sleep(1700);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assertTrue(restClient.fire(tankId).isResult());
    }

    @Test
    public void testGrid_0() {
        when(restClient.grid()).thenReturn(gridWrapper);
        assertNotNull(verify(restClient).grid());
    }

    @Test
    public void testGrid_1() {
        byte dir = 6;
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev = verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assert (prev != verify(restClient).grid());
    }

    @Test
    public void testGrid_2() {
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev = verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assert (prev != verify(restClient).grid());
    }

    @Test
    public void testGrid_3() {
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev = verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assert (prev != verify(restClient).grid());
    }

    @Test
    public void testGrid_4(){
        byte dir = 2;
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        int[][] newGrid = {
                {0, 1000, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1000, 0, 0, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1500, 1500, 0, 1500, 0, 1500, 1500, 1000, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 10000000, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 10010000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 10020002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1001000, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assert (restClient.grid().getGrid() == newGrid);
    }
    @Test
    public void testGrid_5(){
        byte dir = 2;

        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.turn(tankId, (byte) 4)).thenReturn(booleanWrapper);
        int[][] newGrid = {
                {0, 1000, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1000, 0, 0, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1500, 1500, 0, 1500, 0, 1500, 1500, 1000, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 10000000, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 10010000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 10020004, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1001000, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assert (restClient.grid().getGrid() == newGrid);
    }

    @Test
    public void testGrid_6(){
        byte dir = 2;
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.turn(tankId, (byte) 4)).thenReturn(booleanWrapper);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        int[][] newGrid = {
                {0, 1000, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1000, 0, 0, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1000, 1000, 0, 1000, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1500, 1500, 1500, 0, 1500, 0, 1500, 1500, 1000, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 10000000, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 10010000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 10020004, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0,20020101 , 0, 0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1001000, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assert (restClient.grid().getGrid() == newGrid);
    }

    @Test
    public void testRegister_0() {
        when(restClient.register("empascetta", "password")).thenReturn(booleanWrapper);
        assertTrue(restClient.register(anyString(), anyString()).isResult());
    }

    @Test
    public void testLogin_0() {
        when(restClient.login(anyString(), anyString())).thenReturn(longWrapper);
        assertNotNull(restClient.login(anyString(), anyString()));
    }

    @Test
    public void testMove_0() {
        when(restClient.move(anyLong(), anyByte())).thenReturn(booleanWrapper);
        assertTrue(restClient.move(anyLong(), anyByte()).isResult());
    }

    @Test
    public void testLeave_0() {
        when(restClient.leave(anyLong())).thenReturn(booleanWrapper);
        assertTrue(restClient.leave(anyLong()).isResult());
    }
}
