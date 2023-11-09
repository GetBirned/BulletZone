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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;



import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;



public class ClientSideTesting {
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
    public void release(){
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
        assertNotNull(restClient.turn(tankId,dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void validTurn_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId,dir));
        assertTrue(restClient.turn(tankId,dir).isResult());

    }
    @Test
    public void validTurn_3() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId,dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void validTurn_4() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId,dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void validTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
        dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void validTurnSequence_1() {
        byte dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertTrue(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void invalidTurnSequence_0() {
        byte dir = 0;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        dir = 4;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertFalse(restClient.turn(tankId,dir).isResult());
    }
    @Test
    public void invalidTurnSequence_1() {
        byte dir = 6;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        dir = 2;
        when(restClient.turn(tankId, dir)).thenReturn(booleanWrapper);
        assertNotNull(restClient.turn(tankId, dir));
        assertFalse(restClient.turn(tankId,dir).isResult());
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
    public void testGrid_1(){
        byte dir = 6;
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev =  verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.turn(tankId,dir)).thenReturn(booleanWrapper);
        assert(prev != verify(restClient).grid());
    }
    @Test
    public void testGrid_2(){
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev =  verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assert(prev != verify(restClient).grid());
    }
    @Test
    public void testGrid_3(){
        when(restClient.grid()).thenReturn(gridWrapper);
        GridWrapper prev =  verify(restClient).grid();
        assertNotNull(prev);
        when(restClient.fire(tankId)).thenReturn(booleanWrapper);
        assert(prev != verify(restClient).grid());
    }

    @Test
    public void testRegister() {
        when(restClient.register("empascetta", "password")).thenReturn(booleanWrapper);
        assertTrue(restClient.register(anyString(), anyString()).isResult());
    }

    @Test
    public void testLogin() {
        when(restClient.login(anyString(), anyString())).thenReturn(longWrapper);
        assertNotNull(restClient.login(anyString(), anyString()));
    }

    @Test
    public void testMove() {
        when(restClient.move(anyLong(), anyByte())).thenReturn(booleanWrapper);
        assertTrue(restClient.move(anyLong(),anyByte()).isResult());
    }

    @Test
    public void testLeave() {
        when(restClient.leave(anyLong())).thenReturn(booleanWrapper);
        assertTrue(restClient.leave(anyLong()).isResult());
    }


}

