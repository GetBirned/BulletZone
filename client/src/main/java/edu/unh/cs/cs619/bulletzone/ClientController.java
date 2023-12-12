package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient_;
import edu.unh.cs.cs619.bulletzone.util.ArrayListWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import org.androidannotations.rest.spring.annotations.RestService;

@EBean
public class ClientController {

    @RestService
    BulletZoneRestClient restClient;


    public void construct(BulletZoneRestClient restClient) {
        this.restClient = restClient;
    }
    public void setErrorHandler(BZRestErrorhandler z) {
        if (restClient != null) {
            restClient.setRestErrorHandler(z);
        }
    }

    public ArrayListWrapper getPowerups(long tankId, char type){
        return restClient.getPowerups(tankId,type);
    }

    public LongWrapper ejectPowerup(long tankId, char type) {
        return restClient.ejectPowerup(tankId, type);
    }

    public void setTankPowerup(long tankId, int powerupValue, char type) {
        restClient.setTankPowerup(tankId, powerupValue, type);
    }

    public long getResult() {
        return restClient.join().getResult();
    }

    public void moveAsync(long tankId, byte direction) {
        restClient.move(tankId, direction);
    }

    public void turnAsync(long tankId, byte direction) {
        restClient.turn(tankId, direction);
    }

    public LongWrapper deploySoldier(long tankId) {
        return restClient.deploySoldier(tankId);
    }

    public LongWrapper getSoldierHealth(long soldierId) {
        return restClient.getSoldierHealth(soldierId);
    }

    public void fire(long tankId) {
        restClient.fire(tankId);
    }

    public void leave(long tankId) {
        restClient.leave(tankId);
    }

    public void controlBuilder(long tankId) {
        restClient.controlBuilder(tankId);
    }
    public void controlTank(long tankId) {
        restClient.controlTank(tankId);
    }

    public LongWrapper getBuildTime(long tankId) {
        return restClient.getBuildTime(tankId);
    }

    public LongWrapper getDismantleTime(long tankId) {
       return restClient.getDismantleTime(tankId);
    }

    public LongWrapper buildImprovement(int choice, long tankId) {
        return restClient.buildImprovement(choice, tankId);
    }

    public void updateBalance(String receivedTankID, int amount) {
        restClient.updateBalance(receivedTankID, amount);
    }

    public LongWrapper buildTrap(int choice, long tankId) {
        return restClient.buildTrap(choice, tankId);
    }

    public LongWrapper dismantleImprovement(long builderId) {
        return restClient.dismantleImprovement(builderId);
    }

    public LongWrapper getHealth(long tankId) {
        return restClient.getHealth(tankId);
    }

    public IntegerWrapper getBalance(String user) {
        return restClient.getBalance(user);
    }

    public LongWrapper getBuilderHealth(long tankId) {
        return restClient.getBuilderHealth(tankId);
    }
}
