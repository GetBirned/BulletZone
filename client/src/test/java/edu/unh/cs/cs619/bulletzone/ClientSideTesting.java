package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;


import static org.junit.Assert.assertNotNull;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;



public class ClientSideTesting {
    @InjectMocks
    BulletZoneRestClient restClient = Mockito.mock(BulletZoneRestClient.class);
    @Mock
    private RestClientErrorHandling errorHandling;

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
    public void release(){
        reset(restClient);
    }

    @Test
    public void testJoin() {
        when(restClient.join()).thenReturn(longWrapper);
        verify(restClient).join();
    }

    @Test
    public void validTurn_0()  {
        byte dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
    }
    @Test
    public void validTurn_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);

    }
    @Test
    public void validTurn_3() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
    }
    @Test
    public void validTurn_4() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
    }
    @Test
    public void validTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
        dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
    }
    @Test
    public void validTurnSequence_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
    }
    @Test
    public void invalidTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
        dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        BooleanWrapper ret = verify(restClient).turn(tankId,dir);
        assert (!ret.isResult());
    }
    @Test
    public void invalidTurnSequence_1() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        verify(restClient).turn(tankId,dir);
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        BooleanWrapper ret = verify(restClient).turn(tankId,dir);
        assert (!ret.isResult());
    }

    @Test
    public void checkFireTime_0() throws InterruptedException {
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        verify(restClient).fire(tankId);
        Thread.sleep(1000);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        BooleanWrapper ret = verify(restClient).fire(tankId);
        assert (!ret.isResult());
    }
    @Test
    public void checkFireTime_1() throws InterruptedException {
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        verify(restClient).fire(tankId);
        Thread.sleep(1700);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        verify(restClient).fire(tankId);
    }
    @Test
    public void testGrid() {
        when(restClient.grid()).thenReturn(gridWrapper);
        verify(restClient).grid();
    }

    @Test
    public void testRegister() {
        when(restClient.register(anyString(), anyString())).thenReturn(booleanWrapper);
        verify(restClient).register(anyString(), anyString());
    }

    @Test
    public void testLogin() {
        when(restClient.login(anyString(), anyString())).thenReturn(longWrapper);
        verify(restClient).login(anyString(), anyString());
    }

    @Test
    public void testMove() {
        when(restClient.move(anyLong(), anyByte())).thenReturn(booleanWrapper);
        verify(restClient).move(anyLong(), anyByte());
    }

    @Test
    public void testLeave() {
        when(restClient.leave(anyLong())).thenReturn(booleanWrapper);
        verify(restClient).leave(anyLong());
    }
}

