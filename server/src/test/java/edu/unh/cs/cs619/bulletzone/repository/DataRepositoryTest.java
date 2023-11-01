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

import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class DataRepositoryTest {
    @InjectMocks
    DataRepository repo;
    String user;
    String pass;
    String badPass;
    @Before
    public void setUp() throws Exception {
        repo = new DataRepository();
        user = "username";
        pass = "password";
        badPass = "wrongpassword";
    }

    @Test
    public void validateUserTest() throws Exception {
        Assert.assertNotNull(repo.validateUser(user, pass, true));
        Assert.assertNotNull(repo.validateUser(user, pass, true));
        Assert.assertNull(repo.validateUser(user, badPass, true));
        Assert.assertNotNull(repo.validateUser(user, pass, true));
    }


}
