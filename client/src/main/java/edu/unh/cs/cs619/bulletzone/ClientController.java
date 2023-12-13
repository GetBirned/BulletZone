package edu.unh.cs.cs619.bulletzone;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient_;
import edu.unh.cs.cs619.bulletzone.util.ArrayListWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.List;

@EBean
public class ClientController {

    @RestService
    BulletZoneRestClient restClient;



    public void construct(BulletZoneRestClient restClient) {
        this.restClient = restClient;
    }


    public List<Integer> getPowerups(long tankId, char type){
        return restClient.getPowerups(tankId,type);
    }

    public LongWrapper ejectPowerup(long tankId, char type) {
        return restClient.ejectPowerup(tankId, type);
    }

    @Background
    public void setTankPowerup(long tankId, int powerupValue, char type) {
        restClient.setTankPowerup(tankId, powerupValue, type);
    }

    public long getResult() {
        return restClient.join().getResult();
    }

    @Background
    public void moveAsync(long tankId, byte direction) {
        restClient.move(tankId, direction);
    }

    @Background
    public void turnAsync(long tankId, byte direction) {
        restClient.turn(tankId, direction);
    }

    public LongWrapper deploySoldier(long tankId) {
        return restClient.deploySoldier(tankId);
    }

    public LongWrapper getSoldierHealth(long soldierId) {
        return restClient.getSoldierHealth(soldierId);
    }

    @Background
    public void fire(long tankId) {
        restClient.fire(tankId);
    }

    @Background
    public void leave(long tankId) {
        restClient.leave(tankId);
    }

    @Background
    public void controlBuilder(long tankId) {
        restClient.controlBuilder(tankId);
    }
    @Background
    public void controlTank(long tankId) {
        restClient.controlTank(tankId);
    }



    public LongWrapper getDismantleTime(long tankId) {
       return restClient.getDismantleTime(tankId);
    }

    public LongWrapper buildImprovement(int choice, long tankId) {
        return restClient.buildImprovement(choice, tankId);
    }

    @Background
    public void updateBalance(String receivedTankID, int amount) {
        restClient.updateBalance(receivedTankID, amount);
    }

    public LongWrapper buildTrap(int choice, long tankId, int userID) {
        return restClient.buildTrap(choice, tankId, userID);
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


    public int updateBuilderHealthAsync(long tankId) {
        try {
            LongWrapper healthWrapper = restClient.getBuilderHealth(tankId);

            if (healthWrapper != null) {
                Log.e("TAG", "HealthWrapper value: " + healthWrapper.getResult());
                long health = healthWrapper.getResult();
                Log.e("TAG", "Received health from server.getHealth: " + health + "for Builder tank id: " + tankId);
                return (int) health;
            } else {
                Log.e("TAG", "Received null health from server.getHealth for Builder tankId: " + tankId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e("TAG", "Error updating Builder health", e);
        }
        return 0;
    }


    public int updateBankAccountAsync(String user) {
        try {
            // Call your server method to update bank account
            IntegerWrapper updateResult = restClient.getBalance(user);
            //curBalance = updateResult.getValue();
            if (updateResult != null) {
                // Log or handle the update result if needed
                Log.d("TAG", "Bank account updated successfully ");
                //updateBalance(updateResult.getValue());
                return updateResult.getValue();
            } else {
                Log.e("TAG", "Update bank account result is null");
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e("TAG", "Error updating bank account", e);
        }
        return 0;
    }


    public long updateHealthAsync(long tankId) {
        try {
            // Call your server method to get the tank's health
            LongWrapper healthWrapper = restClient.getHealth(tankId);

            if (healthWrapper != null) {
                // Only update the health if it's not null
                Log.e(TAG, "HealthWrapper value: " + healthWrapper.getResult());
                long health = healthWrapper.getResult();
                //updateTankHealth((int) health);
                Log.e(TAG, "Received health from server.getHealth: " + health + "for tank id: " + tankId);
                return health;
            } else {
                Log.e(TAG, "Received null health from server.getHealth for tankId: " + tankId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating tank health", e);
        }
        return 0;
    }


    void leaveAsync(long tankId) {
        System.out.println("Leave called, tank ID: " + tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        restClient.leave(tankId);
    }

    public int ejectPowerupAsync(boolean isSoldierDeployed, long tankId, int controllingTank) {
        LongWrapper result;
        int type = 0;


        if (isSoldierDeployed) {
            result = restClient.ejectPowerup(tankId, 's');
            Log.d("EJECTPOWERUP ------> ", "SOLDIER IS DEPLOYED ");
        } else if(controllingTank == 1) {
            result = restClient.ejectPowerup(tankId, 't');
        } else{
            result = restClient.ejectPowerup(tankId, 'b');
        }
        if (result == null) {
            Log.d(TAG, "ejectPowerupAsync: Result is NULL");
        } else {
            type = (int) result.getResult();
        }


        if (result == null || type == -1) {
            return -1;
        }
        return type;
    }


    protected long deploySoldierAsync(long tankId, long soldierId, boolean isSoldierDeployed) {
        try {
            //if (!isSoldierDeployed) {
            // Attempt to deploy a soldier
            LongWrapper soldierWrapper = restClient.deploySoldier(tankId);

            if (soldierWrapper != null) {
                // Deployment successful
                soldierId = soldierWrapper.getResult();
                Log.d(TAG, "SoldierId is " + soldierId);
                if(soldierId != -1) {
                    return soldierId;
                }

                //updateSoldierHealthAsync(soldierId);

            } else {
                Log.d(TAG, "SoldierId is " + soldierWrapper.getResult() + "\n");
                // Handle other HTTP status codes if needed
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public int updateSoldierHealthAsync(long soldierId) {
        try {
            // Call your server method to get the soldier's health
            LongWrapper healthWrapper = restClient.getSoldierHealth(soldierId);

            if (healthWrapper != null) {
                // Only update the health if it's not null
                long health = healthWrapper.getResult();
                //updateSoldierHealth((int) health);
                Log.e(TAG, "Received health from server.getSoldierHealth: " + health);
                return (int) health;
            } else {
                Log.e(TAG, "Received null health from server.getSoldierHealth for soldierId: " + soldierId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating soldier health", e);
        }
        return 0;
    }


    public boolean dismantleImprovementAsync(long builderId, String receivedTankID, int controllingBuilder) { // REMOVE THE IMPROVEMENT LEFT OF BUILDER
        if (controllingBuilder == 1) {
            if (builderId != -1) {
                LongWrapper res = restClient.dismantleImprovement(builderId);
                if (res != null) {
                    if (res.getResult() == 1) {
                        Log.d(TAG, "Wall properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 100);
                        Log.d(TAG, "100 (Wall) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 2) {
                        Log.d(TAG, "Road properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 40);
                        Log.d(TAG, "40 (Road) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 3) {
                        Log.d(TAG, "Bridge properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 80);
                        Log.d(TAG, "80 (Bridge) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 4) {
                        Log.d(TAG, "AntiGrav properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 300);
                        Log.d(TAG, "300 (AntiGrav) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 5) {
                        Log.d(TAG, "Fusion properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 400);
                        Log.d(TAG, "400 (Fusion) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 6) {
                        Log.d(TAG, "Shield properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 300);
                        Log.d(TAG, "300 (Shield) credits returned to BankAccount with ID: " + builderId + "\n");
                    } else if (res.getResult() == 7) {
                        Log.d(TAG, "ToolKit properly dismantled by: " + builderId + "\n");
                        restClient.updateBalance(receivedTankID, 200);
                        Log.d(TAG, "200 (Toolkit) credits returned to BankAccount with ID: " + builderId + "\n");
                    }

                } else {
                    Log.d(TAG, "Dismantle failed with ID: " + builderId + "\n");
                }
            }
        } else {
            return false;
        }
        return true;
    }


    public long getBuildTime(long tankId) {
        LongWrapper buildTime = restClient.getBuildTime(tankId);

        if (buildTime == null) {
            Log.e(TAG, "buildTime could not be received from server.");
        } else {
            return buildTime.getResult();
        }
        return 0;
    }


    public int buildImprovement(int choice, long builderId, int controllingBuilder, long tankId, String receivedTankID) {
        if (controllingBuilder == 1) {
            LongWrapper res = buildImprovement(choice, tankId);
            if (res != null) {
                if (res.getResult() == 1) {
                    Log.d(TAG, "Wall properly built by ID: " + builderId + "\n");
                    restClient.updateBalance(receivedTankID, -100);
                } else if (res.getResult() == 2) {
                    Log.d(TAG, "Road properly built by ID: " + builderId + "\n");
                    restClient.updateBalance(receivedTankID, -40);
                } else if (res.getResult() == 3) {
                    Log.d(TAG, "Bridge properly built by ID: " + builderId + "\n");
                    restClient.updateBalance(receivedTankID, -80);

                }

            } else {
                Log.d(TAG, "Build failed with ID: " + builderId + "\n");
            }
        } else {
            return -1;

        }
        return 0;
    }


    public int buildTrap(int choice, long soldierId, int controllingTank, long tankId, String receivedTankID, int tankIDFromFile) {
        if (controllingTank == 1) {
            LongWrapper res = restClient.buildTrap(choice, tankId, tankIDFromFile);
            if (res != null) {
                if (res.getResult() == 1) {
                    Log.d(TAG, "Mine properly built by ID: " + tankId + "\n");
                    restClient.updateBalance(receivedTankID, -20);
                } else if (res.getResult() == 2) {
                    Log.d(TAG, "Hijack Trap properly built by ID: " + tankId + "\n");
                    restClient.updateBalance(receivedTankID, -40);
                }

            } else {
                Log.d(TAG, "Trap build failed with ID: " + tankId + "\n");
            }
        } else {
            return -1;

        }
        return 0;
    }
}
