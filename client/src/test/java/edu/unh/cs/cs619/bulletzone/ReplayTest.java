package edu.unh.cs.cs619.bulletzone;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
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

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.ui.ReplayGridAdapter;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;



public class ReplayTest {
    private ReplayActivity replayActivity;
    private ClientActivity clientActivity;
    private ReplayRunner replayRunner;
    private ReplayGridAdapter rGA;
    @InjectMocks
    BulletZoneRestClient restClient = Mockito.mock(BulletZoneRestClient.class);

    @Mock
    private LongWrapper longWrapper;

    @Mock
    private GridWrapper gridWrapper;

    @Mock
    private BooleanWrapper booleanWrapper;


    int tankId = 1;
    String file = replayRunner.getReplayFile();
    @Before
    public void setUp() {
        replayActivity = new ReplayActivity();
        clientActivity = new ClientActivity();
        restClient = clientActivity.restClient;
        replayActivity.replayAdapter = rGA;

    }
    @Test
    public void testUpdateList() {
        int[][] testBoard = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        rGA.updateList(testBoard);

        int[][] updatedBoard = rGA.board;

        assertNotNull(updatedBoard);
        assertArrayEquals(testBoard, updatedBoard);
    }

    @Test
    public void testReplay_0() {
        clientActivity.restClient.move(1, (byte) 6);
        clientActivity.restClient.move(1, (byte) 6);
        clientActivity.restClient.fire(1);
        clientActivity.restClient.turn(1, (byte) 4);

        replayRunner.getReplayStates(file);
        ArrayList<int[][]> boardStates = replayRunner.board_states;

        assertNotNull(boardStates);
        assertFalse(((ArrayList<?>) boardStates).isEmpty());
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 6).isResult()));
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 6).isResult()));
        assert(boardStates.contains(clientActivity.restClient.fire(1).isResult()));
        assert(boardStates.contains(clientActivity.restClient.turn(1, (byte) 4).isResult()));

    }

    @Test
    public void testReplay_1() {
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
        assert (rGA.board== newGrid);
        replayRunner.getReplayStates(file);
        ArrayList<int[][]> boardStates = replayRunner.board_states;
        assertNotNull(boardStates);
        assertFalse(((ArrayList<?>) boardStates).isEmpty());
    }
    @Test
    public void testReplay_2() {
        byte dir = 2;
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.turn(tankId, (byte) 4)).thenReturn(booleanWrapper);
        when(restClient.move(tankId, (byte) 4)).thenReturn(booleanWrapper);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
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
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 10020004, 0, 0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 20020101, 0, 0, 0, 0, 0, 0, 1001000, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 20020101, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assert (rGA.board== newGrid);
        replayRunner.getReplayStates(file);
        ArrayList<int[][]> boardStates = replayRunner.board_states;
        assertNotNull(boardStates);
        assertFalse(((ArrayList<?>) boardStates).isEmpty());
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 2).isResult()));
        assert(boardStates.contains(clientActivity.restClient.turn(1, (byte) 4).isResult()));
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 4).isResult()));
        assert(boardStates.contains(clientActivity.restClient.fire(1).isResult()));
        assert(boardStates.contains(clientActivity.restClient.fire(1).isResult()));
    }
    @Test
    public void testReplay_3() {
        byte dir = 2;
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
        when(restClient.move(tankId, dir)).thenReturn(booleanWrapper);
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
                {0, 0, 0, 10020004, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 20020101, 0, 20020101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1001000, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        assert (rGA.board== newGrid);
        replayRunner.getReplayStates(file);
        ArrayList<int[][]> boardStates = replayRunner.board_states;
        assertNotNull(boardStates);
        assertFalse(((ArrayList<?>) boardStates).isEmpty());
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 2).isResult()));
        assert(boardStates.contains(clientActivity.restClient.fire(1).isResult()));
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 2).isResult()));
        assert(boardStates.contains(clientActivity.restClient.move(1, (byte) 2).isResult()));
        assert(boardStates.contains(clientActivity.restClient.fire(1).isResult()));
    }

}
