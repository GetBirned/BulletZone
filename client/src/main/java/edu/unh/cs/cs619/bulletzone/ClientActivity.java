package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.rest.spring.annotations.RestService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.events.BusProvider;
import edu.unh.cs.cs619.bulletzone.events.ShakeDetector;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;


@EActivity(R.layout.activity_client)
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";

    @Bean
    protected GridAdapter mGridAdapter;

    private Timer healthUpdateTimer;

    @ViewById
    protected GridView gridView;

    String file_timestamp;




    @Bean
    BusProvider busProvider;

    @NonConfigurationInstance
    @Bean
    GridPollerTask gridPollTask;

    EditText editDirection;

    @RestService
    BulletZoneRestClient restClient;

    @Bean
    BZRestErrorhandler bzRestErrorhandler;
    byte previousBuilderDirection = 0;
    byte previousTankDirection = 0;

    /**
     * Remote tank identifier
     */
    private long tankId = -1;

    private int curBalance;

    private long soldierId = -1;

    private long builderId = -1;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    ShakeDetector mShakeDetector;
    @ViewById(R.id.bank_balance)
    TextView bankBalanceTextView;
    int controllingTank;

    int controllingBuilder;
    ButtonController buttonController;
    private int tankIsActive;

    public String receivedTankID;

    public int calledMove;

    public int calledTurn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buttonController = new ButtonController(this);
        // Establish shake/sensorManager. Will handle shakes.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                Log.d(TAG, "Shake initiated, firing bullet");
                onButtonFire();
            }
        });

        // file gets way way way way too large too quickly
        // Delete the file
        //boolean deleted = this.deleteFile("replay_file.txt");
        // sending context over
        mGridAdapter.getContext(this);
        String filename_ts = createNewFile();
        mGridAdapter.setTs(filename_ts);

        // Receiving data in the NextActivity
        Intent receivedIntent = getIntent();
        String recievedTankID = receivedIntent.getStringExtra("username");
        mGridAdapter.setUsername(recievedTankID);
        this.receivedTankID = recievedTankID;


        sensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Click(R.id.ejectPowerup)
    protected void ejectPowerup(){
        ejectPowerupAsync();
    }

    @UiThread
    public void noPowerupToEjectToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    @Background
    protected void ejectPowerupAsync() {
        LongWrapper result;
        int type = 0;


        if (isSoldierDeployed) {
            result = restClient.ejectPowerup(tankId, false);
            Log.d("EJECTPOWERUP ------> ", "SOLDIER IS DEPLOYED ");
        } else {
            result = restClient.ejectPowerup(tankId, true);
        }
        if (result == null) {
            Log.d(TAG, "ejectPowerupAsync: Result is NULL");
        } else {
            type = (int) result.getResult();
        }


        if (result == null || type == -1) {
            noPowerupToEjectToast(this, "No Powerup To Eject!");
        }
        mGridAdapter.didEject = true;
        mGridAdapter.ejectedType = type;
    }


    private String createNewFile() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(ts + ".txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
            Log.d("FILE", "created new file with ts " + ts);
            file_timestamp = ts;
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return ts;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        busProvider.getEventBus().unregister(gridEventHandler);
        sensorManager.unregisterListener(mShakeDetector);
        stopHealthUpdateTimer();
    }

    /**
     * Otto has a limitation (as per design) that it will only find
     * methods on the immediate class type. As a result, if at runtime this instance
     * actually points to a subclass implementation, the methods registered in this class will
     * not be found. This immediately becomes a problem when using the AndroidAnnotations
     * framework as it always produces a subclass of annotated classes.
     *
     * To get around the class hierarchy limitation, one can use a separate anonymous class to
     * handle the events.
     */
    private GridWrapper currentGridWrapper;
    private Object gridEventHandler = new Object()
    {
        @Subscribe
        public void onUpdateGrid(GridUpdateEvent event) {
            if (event.gw != null) {
                currentGridWrapper = event.gw;
                updateGrid(event.gw);
            }
        }

        public GridWrapper getCurrentGridWrapper() {
            return currentGridWrapper;
        }
    };


    @AfterViews
    protected void afterViewInjection() {
        joinAsync();
        SystemClock.sleep(500);
        gridView.setAdapter(mGridAdapter);
        mGridAdapter.setRestClient(restClient);
    }

    private Timer soldierHealthUpdateTimer;

    @AfterInject
    void afterInject() {
        restClient.setRestErrorHandler(bzRestErrorhandler);
        busProvider.getEventBus().register(gridEventHandler);
        startHealthUpdateTimer();
    }

    private void startHealthUpdateTimer() {
        healthUpdateTimer = new Timer();
        healthUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Call the method to update health information
                updateHealthAsync(tankId);
                updateBuilderHealthAsync(tankId);
                //TODO: ADD builder health method here
            }
        }, 0, 1000); // Update health every 5 seconds (adjust the interval as needed)
        soldierHealthUpdateTimer = new Timer();
        soldierHealthUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Call the method to update soldier's health information
                if (isSoldierDeployed) {
                    updateSoldierHealthAsync(soldierId);
                }
            }
        }, 0, 1000);
    }

    private void stopHealthUpdateTimer() {
        if (healthUpdateTimer != null) {
            healthUpdateTimer.cancel();
            healthUpdateTimer = null;
        }
        if (soldierHealthUpdateTimer != null) {
            soldierHealthUpdateTimer.cancel();
            soldierHealthUpdateTimer = null;
        }
    }
    @Background
    void joinAsync() {
        try {
            this.tankId = restClient.join().getResult();
            gridPollTask.doPoll();
            buttonController.initializeButtons();
            controllingTank = 1;
            controllingBuilder = 0;
            buttonController.updateButtons(controllingBuilder);
            tankIsActive = 1;
            updateHealthAsync(tankId);
            updateBuilderHealthAsync(tankId);
        } catch (Exception e) {
            System.out.println("ERROR: joining game");
        }
    }



    public void updateGrid(GridWrapper gw) {
        if (gw != null) {
            mGridAdapter.updateList(gw.getGrid());
        } else {
            Log.e(TAG, "GridWrapper is null");
        }
    }




    byte tempDirection;
    @Click({R.id.buttonUp, R.id.buttonDown, R.id.buttonLeft, R.id.buttonRight})
    protected void onButtonMove(View view) {
        calledMove = 1;
        if (currentlyBuilding == 0) {
            final int viewId = view.getId();
            byte direction = 0;
            final Object lock = new Object();

            switch (viewId) {
                case R.id.buttonUp:
                    direction = 0;
                    tempDirection = 0;
                    //    tankId = 0;
                    break;
                case R.id.buttonDown:
                    direction = 4;
                    tempDirection = 4;
                    //   tankId = 4;
                    break;
                case R.id.buttonLeft:
                    direction = 6;
                    tempDirection = 6;
                    //   tankId = 6;
                    break;
                case R.id.buttonRight:
                    direction = 2;
                    tempDirection = 2;
                    // tankId = 2;
                    break;
                default:
                    Log.e(TAG, "Unknown movement button id: " + viewId);
                    break;
            }
            if (controllingTank == 1) {
                if (previousTankDirection == direction) {
                    previousTankDirection = tempDirection;
                    this.moveAsync(tankId, direction);
                } else {
                    if (previousTankDirection == 2 && direction == 6) {
                        previousTankDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 6 && direction == 2) {
                        previousTankDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 0 && direction == 4) {
                        previousTankDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 4 && direction == 0) {
                        previousTankDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else {
                        previousTankDirection = tempDirection;
                        this.turnAsync(tankId, direction);
                    }
                }
            } else {
                if (previousBuilderDirection == direction) {
                    previousBuilderDirection = tempDirection;
                    this.moveAsync(tankId, direction);
                } else {
                    if (previousBuilderDirection == 2 && direction == 6) {
                        previousBuilderDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 6 && direction == 2) {
                        previousBuilderDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 0 && direction == 4) {
                        previousBuilderDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 4 && direction == 0) {
                        previousBuilderDirection = tempDirection;
                        this.moveAsync(tankId, direction);
                    } else {
                        previousBuilderDirection = tempDirection;
                        this.turnAsync(tankId, direction);
                    }
                }
            }
        }
    }

    @Background
    void moveAsync(long tankId, byte direction) {
        restClient.move(tankId, direction);
    }

    @Background
    void turnAsync(long tankId, byte direction) {
        restClient.turn(tankId, direction);
    }

    @Click(R.id.deploySoldier)
    @Background
    protected void deploySoldier() {
        if (controllingTank == 1) {
            deploySoldierAsync();
        } else {
            showCannotDeployMessage();
        }
    }

    @UiThread
    protected void showCannotDeployMessage() {
        Toast.makeText(getApplicationContext(), "Cannot deploy soldier when controlling Builder. Please switch to Tank then try again.", Toast.LENGTH_SHORT).show();
    }
    private boolean isSoldierDeployed = false;

    protected void deploySoldierAsync() {
        try {
            //if (!isSoldierDeployed) {
            // Attempt to deploy a soldier
            LongWrapper soldierWrapper = restClient.deploySoldier(tankId);

            if (soldierWrapper != null) {
                // Deployment successful
                soldierId = soldierWrapper.getResult();
                isSoldierDeployed = true;

                Log.d(TAG, "SoldierId is " + soldierId);

                updateSoldierHealthAsync(soldierId);

            } else {
                Log.d(TAG, "SoldierId is " + soldierWrapper.getResult() + "\n");
                // Handle other HTTP status codes if needed
            }
            /**
             } else {
             Log.d(TAG, "Soldier already deployed. Cannot deploy another.");
             // Notify the user or handle accordingly
             }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Background
    void updateSoldierHealthAsync(long soldierId) {
        try {
            // Call your restClient method to get the soldier's health
            LongWrapper healthWrapper = restClient.getSoldierHealth(soldierId);

            if (healthWrapper != null) {
                // Only update the health if it's not null
                long health = healthWrapper.getResult();
                updateSoldierHealth((int) health);
                Log.e(TAG, "Received health from restClient.getSoldierHealth: " + health);
            } else {
                Log.e(TAG, "Received null health from restClient.getSoldierHealth for soldierId: " + soldierId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating soldier health", e);
        }
    }

    @UiThread
    public void updateSoldierHealth(int health) {
        TextView soldierHealthTextView = findViewById(R.id.soldierHealth);
        if (health != 0) {
            soldierHealthTextView.setText("" + health);
        } else {
            soldierHealthTextView.setText("N/A"); // or any default value
        }
    }

    // Method to reset soldier status after reentry
    private void resetSoldierStatus() {
        isSoldierDeployed = false;
    }

    @Click(R.id.buttonFire)
    @Background
    protected void onButtonFire() {
        restClient.fire(tankId);
    }

    @Click(R.id.buttonReplay)
    @Background
    void replayButton() {
        Intent intent = new Intent(this, ReplayActivity.class);
        startActivity(intent);
    }

    @Click(R.id.buttonLeave)
    @Background
    void leaveGame() {
        showConfirmationDialog();
    }

    public void performLeave() {
        System.out.println("leaveGame() called, tank ID: "+tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        restClient.leave(tankId);
    }

    @UiThread
    public void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to quit BulletZone?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, proceed with leave action
                        performLeave();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Click(R.id.controlBuilder)
    @Background
    protected void controlBuilder() {
        controllingTank = 0;
        controllingBuilder = 1;
        buttonController.updateButtons(controllingBuilder);
        controlBuilderAsync();
    }

    protected void controlBuilderAsync() {
       LongWrapper res = restClient.controlBuilder(tankId);
    }

    @Click(R.id.controlTank)
    @Background
    protected void controlTank() {
        controllingTank = 1;
        controllingBuilder = 0;
        buttonController.updateButtons(controllingBuilder);
        controlTankAsync();
    }

    protected void controlTankAsync() {
        LongWrapper res = restClient.controlTank(tankId);
    }

    @Click(R.id.buildBridge)
    @Background
    void buildBridge() {
        if (curBalance >= 80) {
            startBuildTimer(3);
            //buildImprovement(3, tankId);
        } else {
            Log.d(TAG, "Bridge could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 80 credits.\n");
        }
    }

    @Click(R.id.buildRoad)
    @Background
    void buildRoad() {
        if (curBalance >= 40) {
            startBuildTimer(2);
            //buildImprovement(2 , tankId);
        } else {
            Log.d(TAG, "Road could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 40 credits.\n");
        }
    }

    @Click(R.id.buildWall)
    @Background
    void buildWall() {
        if (curBalance >= 100) {
            startBuildTimer(1);
            //buildImprovement(1, tankId);
        } else {
            Log.d(TAG, "Wall could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 100 credits.\n");
        }
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

    int currentlyBuilding;

    public void startBuildTimer(int choice) {
        currentlyBuilding = 1;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Check if calledMove is still 0, otherwise, exit the task
                    try {
                        // Ensure UI updates are done on the UI thread
                        buildImprovement(choice, tankId);
                        Log.d(TAG, "buildImprovement executed successfully.\n");
                        currentlyBuilding = 0;
                    } catch (Exception e) {
                        // Handle exceptions if necessary
                        Log.e(TAG, "Error during UI update or buildImprovement", e);
                    } finally {
                        // Optionally, you can cancel the timer task after executing buildImprovement
                        cancel();
                    }
            }
        };

        long buildTime = getBuildTime(tankId);
        if (buildTime != 2000) {
            buildTime = 1000;
        }
        // Schedule the task to run after 2 seconds
        timer.schedule(timerTask, buildTime);
        Log.d(TAG, "Timer started. Waiting to build for " + buildTime + " milliseconds or until calledMove is set to 1...\n");
    }

    public long getDismantleTime(long tankId) {
        LongWrapper dismantleTime = restClient.getDismantleTime(tankId);
        if (dismantleTime == null) {
            Log.e(TAG, "dismantleTime could not be received from server.");
        } else {
            return dismantleTime.getResult();
        }
        return 0;
    }

    public void startDismantleTimer() {
        calledMove = 0;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Check if calledMove is still 0, otherwise, exit the task
                if (calledMove == 0) {
                    try {
                        // Ensure UI updates are done on the UI thread
                        dismantleImprovementAsync(tankId);
                    } catch (Exception e) {
                        // Handle exceptions if necessary
                        Log.e(TAG, "Error during UI update or dismantleImprovement", e);
                    } finally {
                        // Optionally, you can cancel the timer task after executing buildImprovement
                        cancel();
                    }
                } else {
                    Log.d(TAG, "Timer aborted due to calledMove being set to 1.");
                }
            }
        };

        long buildTime = getDismantleTime(tankId);
        //if (buildTime != 2000) {
          //  buildTime = 1000;
        //}
        // Schedule the task to run after 2 seconds
        timer.schedule(timerTask, buildTime);
        Log.d(TAG, "Timer started. Waiting to dismantle for " + buildTime + " milliseconds or until calledMove is set to 1...");
    }

    public void buildImprovement(int choice, long builderId) {
        if (controllingBuilder == 1) {
                LongWrapper res = restClient.buildImprovement(choice, tankId);
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
                    updateBankAccountAsync(receivedTankID);
                } else {
                    Log.d(TAG, "Build failed with ID: " + builderId + "\n");
                }
        } else {
            showCannotBuildMessage();
        }
    }

    @Click(R.id.buildMine)
    @Background
    void buildMine() {
        if (curBalance >= 20) {
            Log.d("MINE", "set mine");
            buildTrap(1, tankId);
        } else {
            Log.d(TAG, "Mine could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 20 credits.\n");
        }
    }

    @Click(R.id.buildHijackTrap)
    @Background
    void buildHijackTrap() {
        if (curBalance >= 40) {
            buildTrap(2 , tankId);
        } else {
            Log.d(TAG, "HijackTrap could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 40 credits.\n");
        }
    }

    public void buildTrap(int choice, long soldierId) {
        if (controllingTank == 1) {
            LongWrapper res = restClient.buildTrap(choice, tankId);
            if (res != null) {
                if (res.getResult() == 1) {
                    Log.d(TAG, "Mine properly built by ID: " + tankId + "\n");
                    restClient.updateBalance(receivedTankID, -20);
                } else if (res.getResult() == 2) {
                    Log.d(TAG, "Hijack Trap properly built by ID: " + tankId + "\n");
                    restClient.updateBalance(receivedTankID, -40);
                }
                updateBankAccountAsync(receivedTankID);
            } else {
                Log.d(TAG, "Trap build failed with ID: " + tankId + "\n");
            }
        } else {
            showCannotBuildTrapMessage();
        }
    }

    @UiThread
    protected void showCannotBuildTrapMessage() {
        Toast.makeText(getApplicationContext(), "Cannot construct trap as builder. Please switch to Soldier then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build while controlling Tank/Soldier. Please switch to Builder then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildOnTopMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build an improvement on top of another improvement / invalid area.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildBridgeMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build bridge when not back is not facing water. Try again.\n", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.dismantleImprovement)
    @Background
    void dismantleImprovement() { // REMOVE THE IMPROVEMENT LEFT OF BUILDER
        startDismantleTimer();
    }

    void dismantleImprovementAsync(long builderId) { // REMOVE THE IMPROVEMENT LEFT OF BUILDER
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
                    }
                   updateBankAccountAsync(receivedTankID);
                } else {
                    Log.d(TAG, "Dismantle failed with ID: " + builderId + "\n");
                }
            }
        } else {
            showCannotDismantleMessage();
        }
    }

    @UiThread
    protected void showCannotDismantleMessage() {
        Toast.makeText(getApplicationContext(), "Cannot dismantle while controlling Tank/Soldier. Please switch to Builder then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    public void updateTankHealth(int health) {
        TextView tankHealthTextView = findViewById(R.id.tankHealth);
        tankHealthTextView.setText("" + health);
    }



    @Background
    void updateHealthAsync(long tankId) {
        try {
            // Call your restClient method to get the tank's health
            LongWrapper healthWrapper = restClient.getHealth(tankId);

            if (healthWrapper != null) {
                // Only update the health if it's not null
                Log.e(TAG, "HealthWrapper value: " + healthWrapper.getResult());
                long health = healthWrapper.getResult();
                updateTankHealth((int) health);
                Log.e(TAG, "Received health from restClient.getHealth: " + health + "for tank id: " + tankId);
                updateBankAccountAsync(receivedTankID);
            } else {
                Log.e(TAG, "Received null health from restClient.getHealth for tankId: " + tankId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating tank health", e);
        }
    }

    @Background
    void updateBankAccountAsync(String user) {
        try {
            // Call your restClient method to update bank account
            IntegerWrapper updateResult = restClient.getBalance(user);
            curBalance = updateResult.getValue();
            if (updateResult != null) {
                // Log or handle the update result if needed
                Log.d(TAG, "Bank account updated successfully for tank id: " + tankId);
                updateBalance(updateResult.getValue());
            } else {
                Log.e(TAG, "Update bank account result is null for tankId: " + tankId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating bank account", e);
        }
    }

    @UiThread
    public void updateBalance(int health) {
        TextView builderHealthTextView = findViewById(R.id.bank_balance);

        builderHealthTextView.setText("" + health);

    }

    @UiThread
    public void updateBuilderHealth(int health) {
        TextView builderHealthTextView = findViewById(R.id.builderHealth);
        if(health <= 0) {
            builderHealthTextView.setText("0");
        } else {
            builderHealthTextView.setText("" + health);
        }
    }


    @Background
    void updateBuilderHealthAsync(long tankId) {
        try {
            // Call your restClient method to get the tank's health
            LongWrapper healthWrapper = restClient.getBuilderHealth(tankId);

            if (healthWrapper != null) {
                // Only update the health if it's not null
                Log.e(TAG, "HealthWrapper value: " + healthWrapper.getResult());
                long health = healthWrapper.getResult();
                updateBuilderHealth((int) health);
                Log.e(TAG, "Received health from restClient.getHealth: " + health + "for Builder tank id: " + tankId);
            } else {
                Log.e(TAG, "Received null health from restClient.getHealth for Builder tankId: " + tankId);
            }
        } catch (Exception e) {
            // Handle the exception
            Log.e(TAG, "Error updating Builder health", e);
        }
    }


    @Background
    void leaveAsync(long tankId) {
        System.out.println("Leave called, tank ID: " + tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        restClient.leave(tankId);
    }
}

